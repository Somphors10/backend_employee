package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.Payroll;
import com.kshrd.admsfileservice.employeemanage.model.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    List<Payroll> findByEmployeeId(UUID employeeId);

    List<Payroll> findByStatus(PayrollStatus status);

    long countByStatus(PayrollStatus status);
}
