package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.Leave;
import com.kshrd.admsfileservice.employeemanage.model.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LeaveRepository extends JpaRepository<Leave, UUID> {
    void deleteByEmployeeId(UUID employeeId);

    long countByStatus(LeaveStatus status);
}
