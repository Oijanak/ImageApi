package com.janak.imageapi.Exception;

import lombok.Data;
import org.springframework.http.HttpStatus;
@Data
public class ErrorResponse {
    private int status;
    private String message;
    private boolean success=false;
    ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
