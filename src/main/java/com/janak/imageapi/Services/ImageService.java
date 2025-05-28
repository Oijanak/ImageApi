package com.janak.imageapi.Services;

import com.janak.imageapi.models.Image;
import com.janak.imageapi.utils.PaginatedResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {
    Image saveImage(MultipartFile image);
    PaginatedResponse<Image> getAllImages(int page, int size);
    Image getImageById(long id);
    Image updateImage(long id,MultipartFile image);
    Resource getImageAsResource(String fileName);
    void deleteImage(long id);

    String getContentType(String imageName);
}
