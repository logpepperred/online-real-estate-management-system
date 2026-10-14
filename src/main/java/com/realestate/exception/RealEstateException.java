package com.realestate.exception;

/**
 * Root custom runtime exception for Real Estate application domain.
 * Demonstrates hierarchical Exception Handling.
 */
public abstract class RealEstateException extends RuntimeException {

    private final String errorCode;

    public RealEstateException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RealEstateException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
