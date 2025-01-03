package com.ssafy.petandpeople.presentation.exceptionhandler;

import com.ssafy.petandpeople.common.error.ErrorCode;
import com.ssafy.petandpeople.presentation.response.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(value = Integer.MAX_VALUE)
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Api<Object>> exceptionHandler(MethodArgumentNotValidException methodArgumentNotValidException) {
        String errorMessage = methodArgumentNotValidException.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

        log.error("MethodArgumentNotValidException occurred : {}", errorMessage);

        return ResponseEntity
                .status(400)
                .body(Api.ERROR(errorMessage));
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Api<Object>> ExceptionHandler(Exception exception) {
        log.error("{}", exception.getMessage(), exception);

        return ResponseEntity
                .status(500)
                .body(Api.ERROR(ErrorCode.SEVER_ERROR.getMessage()));
    }

}