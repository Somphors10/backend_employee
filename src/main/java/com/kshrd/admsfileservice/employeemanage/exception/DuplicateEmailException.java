package com.kshrd.admsfileservice.employeemanage.exception;

public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String email) {
        super("Employee already exists with email: " + email);
    }
}
