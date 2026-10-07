package com.kshrd.admsfileservice.employeemanage.exception;

import java.util.UUID;

public class LeaveNotFoundException extends RuntimeException {
    public LeaveNotFoundException(UUID id) {
        super("Leave request not found with id: " + id);
    }
}
