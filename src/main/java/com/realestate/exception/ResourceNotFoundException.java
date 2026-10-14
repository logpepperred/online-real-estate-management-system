package com.realestate.exception;

public class ResourceNotFoundException extends RealEstateException {

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(resourceName + " not found with identifier: " + identifier, "RESOURCE_NOT_FOUND");
    }
}
