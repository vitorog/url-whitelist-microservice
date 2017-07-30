# url-whitelist-microservice
A URL validation microservice using a range of different technologies 
created for learning purposes.

# Description

An asynchronous message based URL validation microservice. This microservice allows two operations:
insertion and validation.

The operations are performed via async messages (RabbitMQ / AMQP message broker). 

REST endpoints are provided in:
- /urls/insertion
- /urls/validation

The application can be configured via environment variables (see docker-compose.yml)

### Environment variables:

- INSERTION_QUEUE
- VALIDATION_QUEUE
- NUMBER_OF_VALIDATION_CONSUMERS
- RESPONSE_EXCHANGE
- RESPONSE_ROUTING_KEY
- RABBITMQ_HOST
- RABBITMQ_PORT
- RABBITMQ_VHOST
- RABBITMQ_USERNAME
- RABBITMQ_PASSWORD
- JDBC_URL

### Stack used:
* Java 8
* Apache Maven
* Spring Framework
* Spring Boot 
* Spring AMQP
* RabbitMQ
* MySQL
* Docker and Docker Compose
* JUnit

# Instructions

To execute the application run the following commands:
```
mvn clean install
docker-compose up
```