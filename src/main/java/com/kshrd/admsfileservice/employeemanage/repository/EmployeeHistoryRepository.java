package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.EmployeeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeHistoryRepository extends JpaRepository<EmployeeHistory, UUID> {
    List<EmployeeHistory> findByEmployeeIdOrderByOccurredAtDesc(UUID employeeId);
}
