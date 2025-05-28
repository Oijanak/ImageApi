package com.janak.imageapi.utils;

import com.janak.imageapi.models.Image;
import lombok.Data;

@Data
public class ApiResponse{
    private boolean success;
    private Image data;
    public ApiResponse(Image data) {
        success = true;
        this.data = data;
    }
}
