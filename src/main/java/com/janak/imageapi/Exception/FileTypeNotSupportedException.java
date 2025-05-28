package com.janak.imageapi.Exception;

import org.springframework.http.HttpStatus;

public class FileTypeNotSupportedException extends CustomException{
    public FileTypeNotSupportedException() {
        super(HttpStatus.BAD_REQUEST, new ErrorResponse(HttpStatus.BAD_REQUEST,"file type not supported"));
    }
}
