package com.kshrd.admsfileservice.employeemanage.exception;

import java.util.UUID;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(UUID id) {
        super("Employee not found with id: " + id);
    }
}
