package com.kshrd.admsfileservice.employeemanage.repository;

import com.kshrd.admsfileservice.employeemanage.model.entity.OrganizationDepartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationDepartmentRepository extends JpaRepository<OrganizationDepartment, UUID> {
    Optional<OrganizationDepartment> findByNameIgnoreCase(String name);
}
