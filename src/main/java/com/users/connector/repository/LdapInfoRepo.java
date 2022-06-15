package com.users.connector.repository;

import com.users.connector.entity.LdapInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LdapInfoRepo extends CrudRepository<LdapInfo, Integer> {
}
