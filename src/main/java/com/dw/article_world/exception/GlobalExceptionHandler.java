package com.dw.article_world.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({InvalidArticleException.class})
    ResponseEntity<String> handleInvalidArticleIdException(InvalidArticleException invalidArticleException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(invalidArticleException.getMessage());
    }
}
