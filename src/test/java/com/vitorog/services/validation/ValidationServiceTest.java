package com.vitorog.services.validation;

import com.vitorog.configuration.ApplicationConfig;
import com.vitorog.model.GlobalWhitelistEntry;
import com.vitorog.model.GlobalWhitelistEntryDAO;
import com.vitorog.model.WhitelistEntry;
import com.vitorog.model.WhitelistEntryDAO;
import com.vitorog.utils.JSONUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ValidationService.class})
public class ValidationServiceTest {

    @MockBean
    private WhitelistEntryDAO whitelistDAO;

    @MockBean
    private GlobalWhitelistEntryDAO globalWhitelistEntryDAO;

    @MockBean
    private RabbitTemplate template;

    @MockBean
    private ApplicationConfig appConfig;

    @Autowired
    @InjectMocks
    private ValidationService service;

    private List<String> responseExchangeMock;
    private List<WhitelistEntry> validEntries;
    private List<GlobalWhitelistEntry> validGlobalEntries;
    private List<WhitelistEntry> emptyEntries;

    private static final String VALID_REGEX = "(www.google)(\\.\\D+)+";
    private static final String CLIENT = "google";
    private static final String WHITELISTED_URL = "www.google.com";
    private static final String NON_WHITELISTED_URL = "www.gooooogler.com";
    private static final int CORRELATION_ID = 1;

    public ValidationServiceTest() {
    }

    @Before
    public void setUp() {
        responseExchangeMock = new ArrayList<>();

        WhitelistEntry validEntry = new WhitelistEntry(CLIENT, VALID_REGEX);
        validEntries = new ArrayList<>();
        validEntries.add(validEntry);

        validGlobalEntries = new ArrayList<>();
        validGlobalEntries.add(new GlobalWhitelistEntry(validEntry));

        emptyEntries = new ArrayList<>();
    }

    @Test
    public void shouldCorrectlyMatchAWhitelistedURL() throws Exception {
        // Given
        String requestJson = JSONUtil.getJSONFromObject(new ValidationRequest(CLIENT, WHITELISTED_URL, CORRELATION_ID));

        // When
        when(whitelistDAO.findByClient(CLIENT)).thenReturn(validEntries);
        mockResponseSending();
        service.receive(requestJson);

        // Then
        assertMatch(true, VALID_REGEX);
    }

    @Test
    public void shouldCorrectlyMatchANonWhitelistedURL() throws Exception {
        // Given
        String requestJson = JSONUtil.getJSONFromObject(new ValidationRequest(CLIENT, NON_WHITELISTED_URL, CORRELATION_ID));

        // When
        when(whitelistDAO.findByClient(CLIENT)).thenReturn(validEntries);
        mockResponseSending();
        service.receive(requestJson);

        // Then
        assertMatch(false, null);
    }

    @Test
    public void shouldCorrectlySearchInGlobalWhitelist() throws Exception {
        // Given
        String requestJson = JSONUtil.getJSONFromObject(new ValidationRequest(CLIENT, WHITELISTED_URL, CORRELATION_ID));

        // When
        when(whitelistDAO.findByClient(CLIENT)).thenReturn(emptyEntries);
        when(globalWhitelistEntryDAO.findAll()).thenReturn(validGlobalEntries);

        mockResponseSending();
        service.receive(requestJson);

        // Then
        assertMatch(true, VALID_REGEX);
    }

    @Test
    public void shouldNotSendResponseWhenAnInvalidJSONIsReceived() {
        // Given
        String requestJson = JSONUtil.getJSONFromObject("INVALID_JSON");

        // When
        service.receive(requestJson);

        // Then
        verify(template, never()).convertAndSend(any(String.class), any(String.class), any(String.class));
    }

    private void assertMatch(boolean shouldMatch, String regex) {
        Assert.assertEquals(1, responseExchangeMock.size());
        ValidationResponse response = JSONUtil.getObjectFromJSON(responseExchangeMock.get(0), ValidationResponse.class);
        Assert.assertNotNull(response);
        Assert.assertEquals(shouldMatch, response.getMatch());
        Assert.assertEquals(CORRELATION_ID, (long)response.getCorrelationId());
        Assert.assertEquals(regex, response.getRegex());
    }

    private void mockResponseSending() {
        when(appConfig.getResponseExchangeName()).thenReturn("exchange_name");
        when(appConfig.getGetResponseExchangeRoutingKey()).thenReturn("exchange_routing_key");
        Mockito.doAnswer(invocationOnMock -> {
            Object[] responseArray = invocationOnMock.getArguments();

            // RabbitTemplate.convertAndSend(exchange_name, routing_key, message)
            String response = (String)responseArray[2];
            responseExchangeMock.add(response);
            return null;
        }).when(template).convertAndSend(any(String.class), any(String.class), any(String.class));
    }
}