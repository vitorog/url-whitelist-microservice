package com.vitorog.utils;

import com.vitorog.model.WhitelistEntry;
import com.vitorog.services.validation.ValidationRequest;
import com.vitorog.services.validation.ValidationResponse;
import org.junit.Assert;
import org.junit.Test;

public class JSONUtilTest {

    public JSONUtilTest() {
    }

    @Test
    public void shouldReturnNullStringWithInvalidObject() throws Exception {
        String json = JSONUtil.getJSONFromObject(new TestObject("TEST"));

        Assert.assertNull(json);
    }

    @Test
    public void shouldReturnNullObjectWithInvalidJson() throws Exception {
        String invalidJson = "INVALID_JSON";

        WhitelistEntry entry = JSONUtil.getObjectFromJSON(invalidJson, WhitelistEntry.class);
        ValidationResponse response = JSONUtil.getObjectFromJSON(invalidJson, ValidationResponse.class);
        ValidationRequest request = JSONUtil.getObjectFromJSON(invalidJson, ValidationRequest.class);

        Assert.assertNull(entry);
        Assert.assertNull(response);
        Assert.assertNull(request);
    }

    @Test
    public void shouldCorrectlyCreateWhitelistEntryFromJSON() throws Exception {
        // Given
        String validJson = "{\"client\":\"google\", \"regex\":\"(www.google)(\\\\.\\\\D+)+\"}";

        // When
        WhitelistEntry entry = JSONUtil.getObjectFromJSON(validJson, WhitelistEntry.class);

        // Then
        Assert.assertNotNull(entry);
        Assert.assertEquals("google", entry.getClient());
        Assert.assertEquals("(www.google)(\\.\\D+)+", entry.getRegex());
    }

    @Test
    public void shouldCorrectlyCreateJSONFromWhitelistEntry() throws Exception {
        // Given
        WhitelistEntry entry = new WhitelistEntry("google", "(www.google)(\\.\\D+)+");
        String expected = "{\"client\":\"google\",\"regex\":\"(www.google)(\\\\.\\\\D+)+\"}";

        // When
        String json = JSONUtil.getJSONFromObject(entry)
                .replace("\n", "")
                .replace("\r","")
                .replace(" ", "");

        // Then
        Assert.assertNotNull(json);
        Assert.assertEquals(expected, json);
    }

    @Test
    public void shouldCorrectlyParseWhitelistEntry() {
        WhitelistEntry entry = new WhitelistEntry("google", "(www.google)(\\.\\D+)+");

        WhitelistEntry comparisonEntry = JSONUtil.getObjectFromJSON(
                JSONUtil.getJSONFromObject(entry),
                WhitelistEntry.class);

        Assert.assertEquals(entry, comparisonEntry);
    }

    @Test
    public void shouldCorrectlyCreateValidationRequestFromJSON() throws Exception {
        // Given
        String validJson = "{\"client\":\"google\", \"url\":\"www.google.com\", \"correlationId\":0}";

        // When
        ValidationRequest request = JSONUtil.getObjectFromJSON(validJson,ValidationRequest.class);

        // Then
        Assert.assertNotNull(request);
        Assert.assertEquals("google", request.getClient());
        Assert.assertEquals("www.google.com", request.getUrl());
        Assert.assertEquals(0, (long)request.getCorrelationId());
    }

    @Test
    public void shouldCorrectlyCreateJSONFromValidationRequest() throws Exception {
        // Given
        ValidationRequest entry = new ValidationRequest("google", "www.google.com", 0);
        String expected = "{\"client\":\"google\",\"url\":\"www.google.com\",\"correlationId\":0}";

        // When
        String json = JSONUtil.getJSONFromObject(entry)
                .replace("\n", "")
                .replace("\r","")
                .replace(" ", "");

        // Then
        Assert.assertNotNull(json);
        Assert.assertEquals(expected, json);
    }

    @Test
    public void shouldCorrectlyParseValidationRequest() {
        ValidationRequest entry = new ValidationRequest("google", "www.google.com", 0);

        ValidationRequest comparisonEntry = JSONUtil.getObjectFromJSON(
                JSONUtil.getJSONFromObject(entry),
                ValidationRequest.class);

        Assert.assertEquals(entry, comparisonEntry);
    }

    @Test
    public void shouldCorrectlyCreateValidationResponseFromJSON() throws Exception {
        // Given
        String validJson = "{\"match\":\"true\", \"regex\":\"(www.google)\",\"correlationId\":0}";

        // When
        ValidationResponse request = JSONUtil.getObjectFromJSON(validJson,ValidationResponse.class);

        // Then
        Assert.assertNotNull(request);
        Assert.assertTrue(request.getMatch());
        Assert.assertEquals("(www.google)", request.getRegex());
        Assert.assertEquals(0, (long)request.getCorrelationId());
    }

    @Test
    public void shouldCorrectlyCreateJSONFromValidationResponse() throws Exception {
        // Given
        ValidationResponse entry = new ValidationResponse(true, "(www.google)", 0);
        String expected = "{\"match\":true,\"regex\":\"(www.google)\",\"correlationId\":0}";

        // When
        String json = JSONUtil.getJSONFromObject(entry)
                .replace("\n", "")
                .replace("\r","")
                .replace(" ", "");

        // Then
        Assert.assertNotNull(json);
        Assert.assertEquals(expected, json);
    }

    @Test
    public void shouldCorrectlyParseValidationResponse() {
        ValidationResponse response = new ValidationResponse(true, "(www.google)", 0);

        ValidationResponse comparisonResponse = JSONUtil.getObjectFromJSON(
                JSONUtil.getJSONFromObject(response),
                ValidationResponse.class);

        Assert.assertEquals(response, comparisonResponse);
    }

    private class TestObject {
        @SuppressWarnings("unused")
        private String myProperty;

        TestObject(String myProperty) {
            this.myProperty = myProperty;
        }

        // Without getters and setters, Jackson cant parse this
    }
}