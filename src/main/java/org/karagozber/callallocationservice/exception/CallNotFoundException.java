package org.karagozber.callallocationservice.exception;

public class CallNotFoundException extends RuntimeException {
    public CallNotFoundException(String callId) {
        super("No active call found with id '" + callId + "'");
    }
}
