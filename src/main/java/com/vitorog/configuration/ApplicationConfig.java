package com.vitorog.configuration;

import com.vitorog.services.insertion.InsertionService;
import com.vitorog.services.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * Configuration component that setups RabbitMQ.
 *
 * The number of validation consumers is dynamically allocated in the "run" method of the CommandLineRunner interface.
 *
 */
@Configuration
@EnableRabbit
public class ApplicationConfig implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class.getName());

    // The values below are defined in "application.properties"
    @Value("${queue.name.insertion}")
    private String insertionQueueName;

    @Value("${queue.name.validation}")
    private String validationQueueName;

    @Value("${response.exchange.name}")
    private String responseExchangeName;

    @Value("${response.exchange.routing-key}")
    private String responseExchangeRoutingKey;

    @Value("${num.validation-consumers:1}")
    private Integer numValidationConsumers;

    // Access can be package-private
    @Bean(name="InsertionQueueBean")
    Queue insertionQueue() {
        return new Queue(insertionQueueName);
    }

    @Bean(name="ValidationQueueBean")
    Queue validationQueue() {
        return new Queue(validationQueueName);
    }

    @Bean
    DirectExchange responseExchange() { return new DirectExchange(responseExchangeName); }

    @Value("${database.jdbc.url}")
    private String connectionUrl;

    @Bean
    DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(connectionUrl);
        return dataSource;
    }

    @Bean
    InsertionService insertionService() {
        return context.getBeanFactory().createBean(InsertionService.class);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        // Prevents infinite requeue if a message causes an exception
        factory.setErrorHandler(t -> {
            logger.error("Exception in listener", t);
            throw new AmqpRejectAndDontRequeueException(t);
        });
        return factory;
    }

    // Required to control the dynamic allocation of validation consumers
    private ConfigurableApplicationContext context;

    @Autowired
    public ApplicationConfig(ConfigurableApplicationContext context) {
        this.context = context;
    }

    public String getInsertionQueueName() {
        return insertionQueueName;
    }

    public String getValidationQueueName() {
        return validationQueueName;
    }

    public String getResponseExchangeName() {
        return responseExchangeName;
    }

    public String getGetResponseExchangeRoutingKey() {
        return responseExchangeRoutingKey;
    }

    Integer getNumValidationConsumers() {
        return numValidationConsumers;
    }

    String getConnectionURL() {
        return connectionUrl;
    }

    @PostConstruct
    private void logTheseConfigurations(){
        logger.info("Configuration initialized.");
        logger.info("Insertion queue name set to: " + insertionQueueName);
        logger.info("Validation queue name set to: " + validationQueueName);
        logger.info("Response exchange name: " + responseExchangeName);
        logger.info("Response exchange routing key: " + responseExchangeRoutingKey);
        logger.info("Number of validation consumers set to: " + numValidationConsumers);
    }


    // Dynamically allocates the validation consumers. This method is called
    // after all beans are initialized and the spring context has been created.
    @Override
    public void run(String... strings) throws Exception {
        logger.info("Allocating: " + numValidationConsumers);
        for(int i = 0; i < numValidationConsumers; i++) {
            ValidationService service = context.getBeanFactory().createBean(ValidationService.class);
            service.setId(i);
            context.getBeanFactory().registerSingleton("ValidationService_" + i, service);
        }
        RabbitAdmin admin = context.getBean(RabbitAdmin.class);
        admin.initialize();
    }
}
