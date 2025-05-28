package com.janak.imageapi.Controller;

import com.janak.imageapi.Repository.ImageRepository;
import com.janak.imageapi.Services.ImageService;
import com.janak.imageapi.models.Image;
import com.janak.imageapi.utils.ApiResponse;
import com.janak.imageapi.utils.PaginatedResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    ImageController(ImageService imageService, ImageRepository imageRepository) {
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(201).body(new ApiResponse(imageService.saveImage(file)));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<Image>> getAll(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(imageService.getAllImages(page,size));
    }

    @GetMapping("/{id:\\d+}")
    ResponseEntity<ApiResponse> getImageById(@PathVariable long id) {
        return ResponseEntity.ok(new ApiResponse(imageService.getImageById(id)));
    }

    @PutMapping("/{id:\\d+}")
    ResponseEntity<ApiResponse> updateImage(@PathVariable long id, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(new ApiResponse(imageService.updateImage(id, file)));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
      imageService.deleteImage(id);
      return ResponseEntity.noContent().build();
    }

    @GetMapping("/image/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName)  {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imageService.getContentType(imageName)))
                .body(imageService.getImageAsResource(imageName));
    }


}
