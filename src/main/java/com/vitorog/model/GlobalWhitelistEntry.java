package com.vitorog.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;

@Entity
@Table(name="global_whitelist")
public class GlobalWhitelistEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, nullable = false)
    @JsonProperty("regex")
    private String regex;

    public GlobalWhitelistEntry() {
        // Required by JPA
    }

    public GlobalWhitelistEntry(WhitelistEntry entry){
        this.regex = entry.getRegex();
    }

    public String getRegex() {
        return regex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GlobalWhitelistEntry that = (GlobalWhitelistEntry) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return regex != null ? regex.equals(that.regex) : that.regex == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (regex != null ? regex.hashCode() : 0);
        return result;
    }
}
