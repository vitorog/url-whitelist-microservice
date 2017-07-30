package com.vitorog.configuration;

import com.vitorog.model.GlobalWhitelistEntryDAO;
import com.vitorog.model.WhitelistEntryDAO;
import com.vitorog.services.validation.ValidationService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:test.properties")
@SpringBootTest(classes = {ApplicationConfig.class})
public class ApplicationConfigTest {

    @MockBean
    RabbitTemplate template;

    @MockBean
    WhitelistEntryDAO dao;

    @MockBean
    GlobalWhitelistEntryDAO globalDAO;

    @MockBean
    ConnectionFactory connection;

    @Autowired
    SimpleRabbitListenerContainerFactory factory;

    @MockBean
    RabbitAdmin admin;

    @Autowired
    @InjectMocks
    private ApplicationConfig config;

    @Autowired
    private ListableBeanFactory beanFactory;

    private static final int NUM_VALIDATION_CONSUMERS = 25;
    private static final String RESPONSE_ROUTING_KEY = "RESPONSE_ROUTING_KEY";
    private static final String INSERTION_QUEUE = "INSERTION_QUEUE";
    private static final String VALIDATION_QUEUE = "VALIDATION_QUEUE";
    private static final String RESPONSE_EXCHANGE = "RESPONSE_EXCHANGE";
    private static final String JDBC_URL = "JDBC_URL";

    @BeforeClass
    public static void setEnvironment() {
        System.setProperty("NUMBER_OF_VALIDATION_CONSUMERS", String.valueOf(NUM_VALIDATION_CONSUMERS));
        System.setProperty(RESPONSE_ROUTING_KEY, RESPONSE_ROUTING_KEY);
        System.setProperty(INSERTION_QUEUE, INSERTION_QUEUE);
        System.setProperty(VALIDATION_QUEUE, VALIDATION_QUEUE);
        System.setProperty(RESPONSE_EXCHANGE, RESPONSE_EXCHANGE);
        System.setProperty(JDBC_URL, JDBC_URL);
    }

    @Test
    public void shouldCorrectlyReadFilesFromPropertiesFile() {
        Assert.assertEquals(NUM_VALIDATION_CONSUMERS, (long)config.getNumValidationConsumers());
        Assert.assertEquals(RESPONSE_ROUTING_KEY, config.getGetResponseExchangeRoutingKey());
        Assert.assertEquals(INSERTION_QUEUE, config.getInsertionQueueName());
        Assert.assertEquals(VALIDATION_QUEUE, config.getValidationQueueName());
        Assert.assertEquals(RESPONSE_EXCHANGE, config.getResponseExchangeName());
        Assert.assertEquals(JDBC_URL, config.getConnectionURL());
    }

    @Test
    public void shouldInstantiateTheCorrectNumberOfConsumers() throws Exception {
        String[] names = beanFactory.getBeanNamesForType(ValidationService.class);
        Assert.assertNotNull(names);
        Assert.assertEquals(NUM_VALIDATION_CONSUMERS, names.length);
    }
}