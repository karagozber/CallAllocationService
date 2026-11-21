package org.karagozber.callallocationservice.controller;

import org.karagozber.callallocationservice.exception.CallNotFoundException;
import org.karagozber.callallocationservice.exception.NoAvailableNodeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CallAllocationExceptionHandler {
    @ExceptionHandler(CallNotFoundException.class)
    public ResponseEntity<Void> handleCallNotFoundException(CallNotFoundException e) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(NoAvailableNodeException.class)
    public ResponseEntity<Void> handleNoAvailableNodeException(NoAvailableNodeException e) {
        return ResponseEntity.badRequest().build();
    }
}
