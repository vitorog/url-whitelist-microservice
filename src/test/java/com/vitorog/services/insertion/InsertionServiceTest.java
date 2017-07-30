package com.vitorog.services.insertion;

import com.vitorog.model.GlobalWhitelistEntry;
import com.vitorog.model.GlobalWhitelistEntryDAO;
import com.vitorog.model.WhitelistEntry;
import com.vitorog.model.WhitelistEntryDAO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {InsertionService.class})
public class InsertionServiceTest {


    @MockBean
    private WhitelistEntryDAO whitelistDAO;

    @MockBean
    private GlobalWhitelistEntryDAO globalWhitelistEntryDAO;

    @Autowired
    @InjectMocks
    private InsertionService service;
    
    @Test
    public void shouldCorrectlySaveIfValidJSON() throws Exception {
        // Given
        String validJson = "{\"client\": \"google\", \"regex\":\"(www.google)(\\\\.\\\\D+)+\"}";

        // When
       when(whitelistDAO.save(any(WhitelistEntry.class))).thenReturn(null);
       service.receive(validJson);

        // Then
       verify(whitelistDAO).save(any(WhitelistEntry.class));
    }

    @Test
    public void shouldNotSaveWhenAnInvalidJSONIsReceived() throws Exception {
        // Given
        String invalidJson = "{\"unknown_property\": \"google\", \"regex\":\"(www.google)(\\\\.\\\\D+)+\"}";

        // When
        service.receive(invalidJson);

        // Then
        verify(whitelistDAO, never()).save(any(WhitelistEntry.class));
    }

    @Test
    public void shouldCorrectlySaveInGlobalWhiteListIfValidJSONWithNullClient() throws Exception {
        // Given
        String validJson = "{\"client\": null, \"regex\":\"(www.google)(\\\\.\\\\D+)+\"}";

        // When
        when(globalWhitelistEntryDAO.save(any(GlobalWhitelistEntry.class))).thenReturn(null);
        service.receive(validJson);

        // Then
        verify(globalWhitelistEntryDAO).save(any(GlobalWhitelistEntry.class));
    }
}