package com.users.connector.repository;

import com.users.connector.entity.MachineInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MachineInfoRepo extends CrudRepository<MachineInfo, Integer> {
}
