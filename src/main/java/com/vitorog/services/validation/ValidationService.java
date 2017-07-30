package com.vitorog.services.validation;

import com.vitorog.configuration.ApplicationConfig;
import com.vitorog.model.GlobalWhitelistEntry;
import com.vitorog.model.GlobalWhitelistEntryDAO;
import com.vitorog.model.WhitelistEntry;
import com.vitorog.model.WhitelistEntryDAO;
import com.vitorog.utils.JSONUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *  Polls ${VALIDATION_QUEUE} for requests, parses them and queries the database
 *  to check if a URL is valid. The response is sent to exchange ${RESPONSE_EXCHANGE}
 *  with routing key ${RESPONSE_ROUTING_KEY}
 *
 *  Validation requests have the following format: {"client": <string>, "url": <string>, "correlationId": <integer>}
 *  The response has the following format: {"match": <boolean>, "regex": <string/nullable>, "correlationId": <integer>}
 *
 */
@Lazy // This service will be allocated according to the number of consumers configured
@Service
@RabbitListener(queues = "#{@ValidationQueueBean}")
public class ValidationService {

    private static final Logger logger = LoggerFactory.getLogger(ValidationService.class.getName());

    private WhitelistEntryDAO whitelistDAO;

    private GlobalWhitelistEntryDAO globalWhitelistEntryDAO;

    private RabbitTemplate template;

    private ApplicationConfig appConfig;

    private int id;

    @Autowired
    public ValidationService(RabbitTemplate template,
                             ApplicationConfig appConfig,
                             WhitelistEntryDAO whitelistEntryDAO,
                             GlobalWhitelistEntryDAO globalWhitelistEntryDAO) {

        this.template = template;
        this.appConfig = appConfig;
        this.whitelistDAO = whitelistEntryDAO;
        this.globalWhitelistEntryDAO = globalWhitelistEntryDAO;
    }

    public void setId(int id) {
        this.id = id;
    }

    @RabbitHandler
    void receive(String message) {
        logger.info("Received validation request with message: " + message + " in ValidationConsumer_" + id);

        ValidationRequest requestObj = JSONUtil.getObjectFromJSON(message, ValidationRequest.class);
        if(isValidRequest(requestObj)){
            String url = requestObj.getUrl();

            // First we check the clients whitelist
            String matchingRegex = getRegexIfValid(requestObj.getClient(), url);
            if(matchingRegex == null) {
                logger.info("No match found for given client. Checking Global Whitelist...");
                // If no match was found, we check the global whitelist
                matchingRegex = getRegexIfValid(null, url);
            }
            sendResponse(matchingRegex, requestObj.getCorrelationId());
        }
    }

    private String getRegexIfValid(String client, String url) {
        // client = null queries the "global" whitelist
        if(client == null) {
            Iterable<GlobalWhitelistEntry> entries = globalWhitelistEntryDAO.findAll();
            for (GlobalWhitelistEntry entry : entries) {
                if (isValidURL(url, entry.getRegex())) {
                    logger.info("URL is in global whitelisted.");
                    return entry.getRegex();
                }
            }
        }else{
            List<WhitelistEntry> entries = whitelistDAO.findByClient(client);
            for (WhitelistEntry entry : entries) {
                if (isValidURL(url, entry.getRegex())) {
                    logger.info("URL is whitelisted.");
                    return entry.getRegex();
                }
            }
        }
        return null;
    }

    private void sendResponse(String matchingRegex, Integer correlationId) {
        ValidationResponse response = buildResponse(matchingRegex, correlationId);

        String responseJson = JSONUtil.getJSONFromObject(response);
        if(responseJson != null) {
            logger.info("Sending response: " + responseJson);
            template.convertAndSend(appConfig.getResponseExchangeName(),
                    appConfig.getGetResponseExchangeRoutingKey(), responseJson);
        }
    }

    private boolean isValidURL(String url, String regex) {
        return url.matches(regex);
    }

    private boolean isValidRequest(ValidationRequest requestObj) {
        return requestObj != null && requestObj.getClient() != null && requestObj.getUrl() != null;
    }

    private ValidationResponse buildResponse(String regex, Integer correlationId) {
        Boolean isMatch = regex != null;
        return new ValidationResponse(isMatch, regex, correlationId);
    }
}

