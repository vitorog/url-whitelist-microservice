package com.vitorog.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class that converts to / from a Json / Object.
 */
public class JSONUtil {

    private static final Logger logger = LoggerFactory.getLogger(JSONUtil.class.getName());

    private JSONUtil() {
        // Hiding implicit constructor
    }

    /**
     * Maps a JSON string to the given class.
     *
     * @param jsonMessage the JSON message to be mapped
     * @param classType class type to be mapped

     * @return an entity of the given type mapped from the JSON message
     */
    public static  <T> T getObjectFromJSON(String jsonMessage, Class<T> classType) {
        logger.info("Getting JSON from object...");
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(jsonMessage, classType);
        } catch (IOException e) {
            logger.error("Failed to get object from JSON: " + jsonMessage, e);
        }
        return null;
    }

    /**
     * Maps the given object to a JSON.
     *
     * @param object object to be mapped
     * @return a JSON representation of the object
     */
    public static <T> String getJSONFromObject(T object) {
        logger.info("Getting object from JSON...");
        ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            return writer.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Failed to build JSON from object", e);
        }
        return null;
    }
}
