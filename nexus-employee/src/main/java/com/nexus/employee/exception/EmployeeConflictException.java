package com.nexus.employee.exception;

public class EmployeeConflictException extends RuntimeException {

    public EmployeeConflictException(String message) {
        super(message);
    }
}