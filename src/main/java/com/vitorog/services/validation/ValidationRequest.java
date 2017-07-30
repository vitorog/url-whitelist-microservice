package com.vitorog.services.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a request for checking if a URL is whitelisted
 */
public class ValidationRequest {

    @JsonProperty
    private String client;

    @JsonProperty
    private String url;

    @JsonProperty
    private Integer correlationId;

    @SuppressWarnings("unused")
    public ValidationRequest() {
        // Jackson-required
    }

    // JSON annotation used to make parameters mandatory. This enforces valid requests only
    @JsonCreator
    public ValidationRequest(@JsonProperty(value = "client", required = true) String client,
                             @JsonProperty(value = "url", required = true) String url,
                             @JsonProperty(value = "correlationId", required = true) Integer correlationId) {
        this.client = client;
        this.url = url;
        this.correlationId = correlationId;
    }

    public String getClient() {
        return client;
    }

    public String getUrl() {
        return url;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationRequest request = (ValidationRequest) o;

        if (client != null ? !client.equals(request.client) : request.client != null) return false;
        if (url != null ? !url.equals(request.url) : request.url != null) return false;
        return correlationId != null ? correlationId.equals(request.correlationId) : request.correlationId == null;
    }

    @Override
    public int hashCode() {
        int result = client != null ? client.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (correlationId != null ? correlationId.hashCode() : 0);
        return result;
    }
}
