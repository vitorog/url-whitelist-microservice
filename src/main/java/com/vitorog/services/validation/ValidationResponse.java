package com.vitorog.services.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO representing a validation response.
 */
public class ValidationResponse {

    @JsonProperty(required = true)
    private Boolean match;

    @JsonProperty(required = true)
    private String regex;

    @JsonProperty(required = true)
    private Integer correlationId;

    @SuppressWarnings("unused")
    public ValidationResponse() {
        // Jackson-required
    }

    public ValidationResponse(Boolean match, String regex, Integer correlationId) {
        this.match = match;
        this.regex = regex;
        this.correlationId = correlationId;
    }

    public Boolean getMatch() {
        return match;
    }

    public String getRegex() {
        return regex;
    }

    public Integer getCorrelationId() {
        return correlationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationResponse that = (ValidationResponse) o;

        if (match != null ? !match.equals(that.match) : that.match != null) return false;
        if (regex != null ? !regex.equals(that.regex) : that.regex != null) return false;
        return correlationId != null ? correlationId.equals(that.correlationId) : that.correlationId == null;
    }

    @Override
    public int hashCode() {
        int result = match != null ? match.hashCode() : 0;
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        result = 31 * result + (correlationId != null ? correlationId.hashCode() : 0);
        return result;
    }
}
