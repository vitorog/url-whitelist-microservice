package com.vitorog.services.insertion;

import com.vitorog.model.GlobalWhitelistEntry;
import com.vitorog.model.GlobalWhitelistEntryDAO;
import com.vitorog.model.WhitelistEntry;
import com.vitorog.model.WhitelistEntryDAO;
import com.vitorog.utils.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 *  Polls ${INSERTION_QUEUE} for requests, parses them and inserts a WhitelistEntry in
 *  the database if the request is valid.
 *
 *  Insertion requests have the following format: {"client": <string/nullable>, "regex": <string>}
 */
@Service
@Lazy
@RabbitListener(queues = "#{@InsertionQueueBean}")
public class InsertionService {

    private static final Logger logger = LoggerFactory.getLogger(InsertionService.class.getName());

    private WhitelistEntryDAO whitelistDAO;

    private GlobalWhitelistEntryDAO globalWhitelistDAO;

    @Autowired
    public InsertionService(WhitelistEntryDAO whitelistDAO, GlobalWhitelistEntryDAO globalWhitelistDAO) {
        this.whitelistDAO = whitelistDAO;
        this.globalWhitelistDAO = globalWhitelistDAO;
    }

    @RabbitHandler
    void receive(String message) {
        logger.info("Received insertion request with message: " + message);

        WhitelistEntry entry = JSONUtil.getObjectFromJSON(message, WhitelistEntry.class);
        if(entry != null) {
            insertEntity(entry);
        }
    }

    private void insertEntity(WhitelistEntry entry) {
        logger.info("Inserting entity: " + entry.toString());
        try {
            if(entry.getClient() != null) {
                whitelistDAO.save(entry);
            }else{
                logger.info("Saving to Global Whitelist...");
                globalWhitelistDAO.save(new GlobalWhitelistEntry(entry));
            }
            logger.info("Inserted.");
        } catch (DataIntegrityViolationException e) {
            logger.warn("Unable to insert entity: " + entry.toString(), e);
        }
    }
}

