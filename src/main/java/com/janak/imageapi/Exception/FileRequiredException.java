package com.janak.imageapi.Exception;

import org.springframework.http.HttpStatus;

public class FileRequiredException extends CustomException{
    public FileRequiredException() {
        super(HttpStatus.BAD_REQUEST, new ErrorResponse(HttpStatus.BAD_REQUEST.value(),"File is required"));
    }
}
