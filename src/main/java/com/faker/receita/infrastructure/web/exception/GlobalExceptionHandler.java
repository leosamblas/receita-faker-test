package com.faker.receita.infrastructure.web.exception;

import java.time.Clock;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.faker.receita.domain.exception.DomainException;
import com.faker.receita.domain.exception.InvalidDocumentException;

import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    public GlobalExceptionHandler() {
        this(Clock.systemDefaultZone());
    }

    @ExceptionHandler(InvalidDocumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInvalidDocumentException(
            InvalidDocumentException ex,
            ServerHttpRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getPath().value(),
                clock
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainException(
            DomainException ex,
            ServerHttpRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.UNPROCESSABLE_CONTENT.value(),
                HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase(),
                ex.getMessage(),
                request.getPath().value(),
                clock
        );
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(errorResponse));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerHttpRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getPath().value(),
                clock
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }
}
