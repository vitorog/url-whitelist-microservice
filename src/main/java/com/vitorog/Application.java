package com.vitorog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entry point for the application. Before Spring application is started,
 * it waits until a connection to the MySQL DB and RabbitMQ server is successfully.
 *
 * For proper initialization, the following environment variables have to be defined:
 * JDBC_URL
 * RABBITMQ_HOST
 * RABBITMQ_USERNAME
 * RABBITMQ_PASSWORD
 *
 */
@SpringBootApplication
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class.getName());

    private static final int WAIT_DELAY = 5000;

    private static void waitForMySQLInit() throws InterruptedException {

        Connection connection = null;
        try {
            boolean connected = false;
            while (!connected) {
                logger.info("Checking for MySQL connection...");
                String url = System.getenv("JDBC_URL");
                connection = DriverManager.getConnection(url);
                connected = true;
                logger.info("Success!");
            }
        } catch (SQLException e) {
            // Fail!
            logger.warn("Could not connect to MySQL. Retrying...", e);
        } finally {
            if (connection != null){
                try {
                    connection.close();
                } catch (SQLException ignore) {
                    // Do nothing
                }
            }
        }
        sleep();
    }

    private static void waitForRabbitMQInit() throws InterruptedException {
        boolean connected = false;
        while(!connected) {
            logger.info("Checking for RabbitMQ connection...");
            CachingConnectionFactory connection = new CachingConnectionFactory();
            try {
                String host = System.getenv("RABBITMQ_HOST");
                String user = System.getenv("RABBITMQ_USERNAME");
                String password = System.getenv("RABBITMQ_PASSWORD");
                connection.setHost(host);
                connection.setUsername(user);
                connection.setPassword(password);
                connection.createConnection();

                connected = true;
                logger.info("Success!");
                break;
            } catch (AmqpConnectException e) {
                logger.warn("Could not connect to RabbitMQServer. Retrying...", e);
            } finally {
                connection.resetConnection();
            }
            sleep();
        }
    }

    private static void sleep() throws InterruptedException {
        try {
            Thread.sleep(WAIT_DELAY);
        } catch (InterruptedException e) {
            logger.error("Exception while sleeping", e);
            throw e;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        waitForMySQLInit();
        waitForRabbitMQInit();
        SpringApplication.run(Application.class, args);
    }
}
