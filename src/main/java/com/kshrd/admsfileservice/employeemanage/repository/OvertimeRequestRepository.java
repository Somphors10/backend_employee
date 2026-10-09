package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.OvertimeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, UUID> {
    List<OvertimeRequest> findByEmployeeId(UUID employeeId);
}
