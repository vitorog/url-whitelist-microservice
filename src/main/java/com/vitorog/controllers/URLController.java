package com.vitorog.controllers;

import com.vitorog.configuration.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint that can be used to access the microservice. *
 */
@RestController
@RequestMapping("/urls")
public class URLController {

    private static final Logger logger = LoggerFactory.getLogger(URLController.class.getName());

    private RabbitTemplate template;

    private ApplicationConfig appConfig;

    @Autowired
    public URLController(RabbitTemplate template, ApplicationConfig appConfig) {
        this.template = template;
        this.appConfig = appConfig;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/insertion")
    public ResponseEntity insert(@RequestBody String request){
        logger.info("Received insert request: " + request);
        this.template.convertAndSend(appConfig.getInsertionQueueName(), request);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/validation")
    public ResponseEntity validate(@RequestBody String request){
        logger.info("Received validation request: " + request);

        this.template.convertAndSend(appConfig.getValidationQueueName(), request);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}
