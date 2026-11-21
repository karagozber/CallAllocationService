package org.karagozber.callallocationservice.exception;

public class NoAvailableNodeException extends RuntimeException {
    public NoAvailableNodeException(String region) {
        super("No available node with free capacity in region '" + region + "'");
    }
}
