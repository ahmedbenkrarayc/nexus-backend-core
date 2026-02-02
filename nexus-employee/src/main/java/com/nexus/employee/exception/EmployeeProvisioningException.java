package com.nexus.employee.exception;

public class EmployeeProvisioningException extends RuntimeException {

    public EmployeeProvisioningException(String message) {
        super(message);
    }

    public EmployeeProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}