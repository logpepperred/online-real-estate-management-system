package com.realestate.exception;

public class UnauthorizedActionException extends RealEstateException {

    public UnauthorizedActionException(String message) {
        super(message, "UNAUTHORIZED_ACTION");
    }
}
