package com.yieldlab.gdpr.exception;

public class GdprConsentMissingException extends GdprException {
    public GdprConsentMissingException(String message) {
        super(message);
    }

    public GdprConsentMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
