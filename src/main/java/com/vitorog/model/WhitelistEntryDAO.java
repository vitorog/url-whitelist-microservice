package com.vitorog.model;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhitelistEntryDAO extends CrudRepository<WhitelistEntry, Long>
{
    List<WhitelistEntry> findByClient(String client);
}
