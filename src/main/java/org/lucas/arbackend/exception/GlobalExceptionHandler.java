package org.lucas.arbackend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorDetailsResponse> handleNotFound(EntityNotFoundException ex, WebRequest request) {

        log.error("Entity Not Found Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.NOT_FOUND)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDetailsResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Illegal Argument Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDetailsResponse> handleValidation(ValidationException ex, WebRequest request) {

        log.warn("Validation Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.UNPROCESSABLE_ENTITY)
                .build();

        return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorDetailsResponse> handleIllegalState(IllegalStateException ex, WebRequest request) {

        log.error("Illegal State Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.FAILED_DEPENDENCY)
                .build();
        return new ResponseEntity<>(response, HttpStatus.FAILED_DEPENDENCY);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetailsResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
        log.warn("Method Argument Not Validation Exception: ", ex);

        Map<String, String> errorsMap = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            errorsMap.put(error.getCode(), error.getDefaultMessage());
                });

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(errorsMap.toString())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.NOT_ACCEPTABLE)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(TypeNotPresentException.class)
    public ResponseEntity<ErrorDetailsResponse> handleTypeNotPresent(TypeNotPresentException ex, WebRequest request) {

        log.warn("Type Not Present Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.BAD_REQUEST)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorDetailsResponse> handleSecurity(AccessDeniedException ex, WebRequest request) {

        log.warn("Access Denied Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMessage())
                .details(request.getDescription(false))
                .errorCode(HttpStatus.UNAUTHORIZED)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorDetailsResponse> handlePropertyReference(PropertyReferenceException ex, WebRequest request) {
        log.warn("Property Reference Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
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
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message(ex.getMostSpecificCause().getMessage().concat(" Please contact support."))
                .details(request.getDescription(false))
                .errorCode(HttpStatus.CONFLICT)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDetailsResponse> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {

        log.error("Database Constraint Violation Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
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
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message("An unexpected error occurred. Our team has been notified. Please try again later.")
                .details(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ErrorDetailsResponse> handleInvalidDataAccessResourceUsage(InvalidDataAccessResourceUsageException ex, WebRequest request) {
        log.error("Invalid Data Access Resource Usage Exception: ", ex);

        ErrorDetailsResponse response = ErrorDetailsResponse.builder()
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message("An unexpected error occurred. Our team has been notified. Please try again later.")
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
                .timeStamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .message("An unexpected error occurred. Our team has been notified. Please try again later.")
                .details(request.getDescription(false))
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
