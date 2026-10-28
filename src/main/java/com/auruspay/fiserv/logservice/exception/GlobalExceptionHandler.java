package com.auruspay.logservice.exception;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.core.JsonParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<?> handleDataNotFoundException(NoDataFoundException ex) {
        log.error("Data Not Found Exception", ex);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "status", "NOT_FOUND",
                        "message", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<?> handleJsonParseException(JsonParseException ex) {
        log.error("JSON Parse Exception", ex);
        String errorMessage = ex.getMessage();

        // Handle NO_DATA_FOUND specifically
        if (errorMessage != null && errorMessage.contains("NO_DATA_FOUND")) {
            return ResponseEntity.ok(
                    Map.of(
                            "status", "NO_DATA_FOUND",
                            "message", errorMessage
                    )
            );
        }

        return ResponseEntity.badRequest().body(
                Map.of(
                        "status", "error",
                        "errorType", "JsonParseException",
                        "message", ex.getOriginalMessage()
                )
        );
    }
}