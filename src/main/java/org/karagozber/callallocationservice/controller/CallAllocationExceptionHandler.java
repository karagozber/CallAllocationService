package org.karagozber.callallocationservice.controller;

import org.karagozber.callallocationservice.exception.CallNotFoundException;
import org.karagozber.callallocationservice.exception.NoAvailableNodeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CallAllocationExceptionHandler {
  @ExceptionHandler(CallNotFoundException.class)
  public ProblemDetail handleCallNotFoundException(CallNotFoundException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(NoAvailableNodeException.class)
  public ProblemDetail handleNoAvailableNodeException(NoAvailableNodeException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
