package com.vitorog.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="whitelist", uniqueConstraints = @UniqueConstraint(columnNames = {"client", "regex"}))
public class WhitelistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(max = 128)
    @Column(nullable = false)
    @JsonProperty("client")
    private String client;

    @Column(nullable = false)
    @JsonProperty("regex")
    private String regex;

    public WhitelistEntry() {
        // Required by JPA
    }

    public WhitelistEntry(String client, String regex) {
        this.client = client;
        this.regex = regex;
    }

    public String getRegex() {
        return regex;
    }

    public String getClient() {
        return client;
    }

    @Override
    public String toString() {
        return "WhitelistEntry{" +
                "client='" + client + '\'' +
                ", regex='" + regex + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WhitelistEntry that = (WhitelistEntry) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (client != null ? !client.equals(that.client) : that.client != null) return false;
        return regex != null ? regex.equals(that.regex) : that.regex == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (client != null ? client.hashCode() : 0);
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        return result;
    }
}
