package org.lucas.arbackend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDetailsResponse> handleNotFound(EntityNotFoundException ex, WebRequest request) {

        log.error("Entity Not Found Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetailsResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {

        log.error("Illegal State Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.FAILED_DEPENDENCY)
                .build();
        return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetailsResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {

        log.error("Method Argument Not Validation Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorDetailsResponse> handleSecurity(AccessDeniedException ex, WebRequest request) {

        log.error("Access Denied Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.UNAUTHORIZED)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorDetailsResponse> handlePropertyReference(PropertyReferenceException ex, WebRequest request) {
        log.error("Property Reference Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    // Database Errors which get thrown automatically
    // Set up a friendly message as not to expose the infrastructure using .getMessage()
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDetailsResponse> handleConflict(DataIntegrityViolationException ex, WebRequest request) {

        log.error("Database Integrity Violation Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.CONFLICT)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetailsResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        log.error("Database Constraint Violation Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDetailsResponse> handleDatabaseGeneral(DataAccessException ex, WebRequest request) {

        log.error("Database Access Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Handle unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetailsResponse> handleGeneral(Exception ex, WebRequest request) {

        log.error("General Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now())
                .message("An unexpected error occurred. Our team has been notified. Please try again later.")
                .details(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
