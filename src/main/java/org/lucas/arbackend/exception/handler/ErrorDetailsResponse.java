package org.lucas.arbackend.exception.handler;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data @Builder
public class ErrorDetailsResponse {
    private LocalDateTime timeStamp;
    private String message;
    private String details;
    private HttpStatus errorCode;
}
