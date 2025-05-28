package com.janak.imageapi.Exception;

import org.springframework.http.HttpStatus;

public class ImageNotFoundException extends CustomException{
    public ImageNotFoundException() {
        super(HttpStatus.NOT_FOUND,new ErrorResponse(HttpStatus.NOT_FOUND,"Image does not exist"));
    }
}
