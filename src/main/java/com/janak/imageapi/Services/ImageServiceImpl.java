package com.janak.imageapi.Services;

import com.janak.imageapi.Exception.FileTypeNotSupportedException;
import com.janak.imageapi.Exception.ImageNotFoundException;
import com.janak.imageapi.Repository.ImageRepository;
import com.janak.imageapi.models.Image;

import com.janak.imageapi.utils.FileUtils;
import com.janak.imageapi.utils.PaginatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@Service
public class ImageServiceImpl implements ImageService {
    @Value("${file.upload-dir}")
    private String uploadPath;

    private final ImageRepository imageRepository;

    public ImageServiceImpl(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public Image saveImage(MultipartFile image) {
        validateImageFile(image);
        String extension = Objects.requireNonNull(image.getOriginalFilename()).substring(image.getOriginalFilename().lastIndexOf("."));
        String newImageName = System.currentTimeMillis() + "_" + UUID.randomUUID()+extension;
        Path filePath = Paths.get(uploadPath, newImageName);
        try {
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, image.getBytes());
            Image imageToSave=Image.builder()
                    .imageName(newImageName)
                    .imageType(image.getContentType())
                    .imageSize(FileUtils.getReadableFileSize(image.getSize()))
                    .build();

        return imageRepository.save(imageToSave);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PaginatedResponse<Image> getAllImages(int page, int size) {

        Pageable pageable = PageRequest.of(page-1, size);
        Page<Image> pageImages = imageRepository.findAll(pageable);

        PaginatedResponse<Image> response = new PaginatedResponse<>();
        response.setData(pageImages.getContent());
        response.setCurrentPage(pageImages.getNumber()+1);
        response.setTotalPages(pageImages.getTotalPages());
        response.setTotalItems(pageImages.getTotalElements());
        response.setLast(pageImages.isLast());
        response.setFirst(pageImages.isFirst());

        return response;
    }

    @Override
    public Image getImageById(long id) {
        Image image = imageRepository.findById(id).orElse(null);
        if(image == null) {
            throw new ImageNotFoundException();
        }
        return image;
    }

    @Override
    public Image updateImage(long id, MultipartFile image) {
        Optional<Image> existingImageOpt = imageRepository.findById(id);
        if (existingImageOpt.isEmpty()) {
            throw new ImageNotFoundException();
        }
        Image existingImage = existingImageOpt.get();
        try {
            Files.deleteIfExists(Paths.get(uploadPath,existingImage.getImageName()));
            String extension = Objects.requireNonNull(image.getOriginalFilename()).substring(image.getOriginalFilename().lastIndexOf("."));
            String newImageName = System.currentTimeMillis() + "_" + UUID.randomUUID()+extension;
            Path filePath = Paths.get(uploadPath, newImageName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, image.getBytes());
            existingImage.setImageName(newImageName);
            existingImage.setImageType(image.getContentType());
            existingImage.setImageSize(FileUtils.getReadableFileSize(image.getSize()));
            existingImage.setUploadedAt(Date.from(Instant.now()));
            return imageRepository.save(existingImage);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Resource getImageAsResource(String fileName) {
        Path path = Paths.get(uploadPath).resolve(fileName);
        Resource resource = null;
        try {
            resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ImageNotFoundException();
            }
        } catch (MalformedURLException e) {
            throw new ImageNotFoundException();
        }
        return resource;

    }

    @Override
    public void deleteImage(long id) {
        Optional<Image> image = imageRepository.findById(id);
        if (image.isEmpty()) {
            throw new ImageNotFoundException();
        }
        try {
            Files.deleteIfExists(Paths.get(uploadPath,image.get().getImageName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        imageRepository.deleteById(id);
    }


    public void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String name = file.getOriginalFilename();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileTypeNotSupportedException();
        }

        if (name == null || !name.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            throw new FileTypeNotSupportedException();
        }
    }

    public String getContentType(String imageName) {
        Path path = Paths.get(uploadPath).resolve(imageName);
        try {
            return Files.probeContentType(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
