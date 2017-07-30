package com.vitorog.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalWhitelistEntryDAO extends CrudRepository<GlobalWhitelistEntry, Long> {
}
