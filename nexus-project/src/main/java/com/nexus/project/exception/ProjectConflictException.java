package com.nexus.project.exception;

public class ProjectConflictException extends RuntimeException {
    public ProjectConflictException(String message) {
        super(message);
    }

    public ProjectConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
