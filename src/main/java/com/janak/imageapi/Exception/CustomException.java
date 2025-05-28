package com.janak.imageapi.Exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class CustomException extends RuntimeException {
    private HttpStatus status;
    private ErrorResponse response;
    public CustomException(HttpStatus status,ErrorResponse response) {
        this.status = status;
        this.response = response;
    }
}
