package com.myrrhax.deviceservice.controller;

import com.myrrhax.deviceservice.exception.ApplicationException;
import com.myrrhax.deviceservice.exception.DeviceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DeviceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleDeviceNotFoundException(DeviceNotFoundException ex) {
        log.error("Device with id {} is not found", ex.getId());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle(ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problemDetail);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApplicationException(ApplicationException ex) {
        log.error("An error occurred processing the request {}", ex.getMessage(), ex);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }
}
