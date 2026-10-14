package com.realestate.exception;

public class DuplicateResourceException extends RealEstateException {

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE");
    }
}
