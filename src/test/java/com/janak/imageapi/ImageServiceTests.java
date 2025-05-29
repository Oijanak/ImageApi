package com.janak.imageapi;

import com.janak.imageapi.Exception.FileRequiredException;
import com.janak.imageapi.Exception.FileTypeNotSupportedException;
import com.janak.imageapi.Exception.ImageNotFoundException;
import com.janak.imageapi.Repository.ImageRepository;
import com.janak.imageapi.Services.ImageServiceImpl;
import com.janak.imageapi.models.Image;
import com.janak.imageapi.utils.FileUtils;
import com.janak.imageapi.utils.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    private final String uploadPath = "test-uploads";
    private MultipartFile validImageFile;
    private MultipartFile invalidImageFile;
    private MultipartFile emptyFile;
    private Image testImage;

    @BeforeEach
    void setUp() {
        imageService = new ImageServiceImpl(imageRepository,"test-uploads");


        validImageFile = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );

        invalidImageFile = new MockMultipartFile(
                "image",
                "test.txt",
                "text/plain",
                "invalid content".getBytes()
        );

        emptyFile = new MockMultipartFile(
                "image",
                "empty.png",
                "image/png",
                new byte[0]
        );

        testImage = Image.builder()
                .id(1L)
                .imageName("test.png")
                .imageType("image/png")
                .imageSize("10 KB")
                .build();
    }

    @Test
    void saveImage_WithValidImage_ShouldSaveSuccessfully() throws IOException {
        // Arrange
        when(imageRepository.save(any(Image.class))).thenReturn(testImage);

        // Act
        Image savedImage = imageService.saveImage(validImageFile);

        // Assert
        assertNotNull(savedImage);
        assertEquals(testImage.getId(), savedImage.getId());
        verify(imageRepository, times(1)).save(any(Image.class));

        // Clean up
        Files.deleteIfExists(Paths.get(uploadPath, testImage.getImageName()));
    }

    @Test
    void saveImage_WithNullFile_ShouldThrowFileRequiredException() {
        // Act & Assert
        assertThrows(FileRequiredException.class, () -> imageService.saveImage(null));
    }

    @Test
    void saveImage_WithEmptyFile_ShouldThrowFileRequiredException() {
        // Act & Assert
        assertThrows(FileRequiredException.class, () -> imageService.saveImage(emptyFile));
    }

    @Test
    void saveImage_WithInvalidFileType_ShouldThrowFileTypeNotSupportedException() {
        // Act & Assert
        assertThrows(FileTypeNotSupportedException.class, () -> imageService.saveImage(invalidImageFile));
    }

    @Test
    void getAllImages_ShouldReturnPaginatedResponse() {
        // Arrange
        List<Image> images = Arrays.asList(testImage, testImage, testImage);
        Page<Image> page = new PageImpl<>(images);
        when(imageRepository.findAll(any(Pageable.class))).thenReturn(page);

        // Act
        PaginatedResponse<Image> response = imageService.getAllImages(1, 10);

        // Assert
        assertNotNull(response);
        assertEquals(3, response.getData().size());
        assertEquals(1, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(3, response.getTotalItems());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void getImageById_WithExistingId_ShouldReturnImage() {
        // Arrange
        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));

        // Act
        Image foundImage = imageService.getImageById(1L);

        // Assert
        assertNotNull(foundImage);
        assertEquals(testImage.getId(), foundImage.getId());
    }

    @Test
    void getImageById_WithNonExistingId_ShouldThrowImageNotFoundException() {
        // Arrange
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.getImageById(999L));
    }

    @Test
    void updateImage_WithValidData_ShouldUpdateSuccessfully() throws IOException {
        // Arrange
        Image updatedImage = Image.builder()
                .id(1L)
                .imageName("updated.png")
                .imageType("image/png")
                .imageSize("15 KB")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        when(imageRepository.save(any(Image.class))).thenReturn(updatedImage);

        // Act
        Image result = imageService.updateImage(1L, validImageFile);

        // Assert
        assertNotNull(result);
        assertEquals(updatedImage.getImageName(), result.getImageName());
        verify(imageRepository, times(1)).save(any(Image.class));

        // Clean up
        Files.deleteIfExists(Paths.get(uploadPath, updatedImage.getImageName()));
    }

    @Test
    void updateImage_WithNonExistingId_ShouldThrowImageNotFoundException() {
        // Arrange
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.updateImage(999L, validImageFile));
    }

    @Test
    void updateImage_WithNullFile_ShouldThrowFileRequiredException() {
        // No need to mock repository call since exception is thrown before it's reached
        assertThrows(FileRequiredException.class, () -> imageService.updateImage(1L, null));
    }

    @Test
    void getImageAsResource_WithExistingFile_ShouldReturnResource() throws IOException {
        // Arrange
        Path testFilePath = Paths.get(uploadPath, testImage.getImageName());
        Files.createDirectories(testFilePath.getParent());
        Files.write(testFilePath, "test content".getBytes());

        // Act
        Resource resource = imageService.getImageAsResource(testImage.getImageName());

        // Assert
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());

        // Clean up
        Files.deleteIfExists(testFilePath);
    }

    @Test
    void getImageAsResource_WithNonExistingFile_ShouldThrowImageNotFoundException() {
        // Act & Assert
        assertThrows(ImageNotFoundException.class,
                () -> imageService.getImageAsResource("nonexistent.png"));
    }

    @Test
    void deleteImage_WithExistingId_ShouldDeleteSuccessfully() throws IOException {
        // Arrange
        Path testFilePath = Paths.get(uploadPath, testImage.getImageName());
        Files.createDirectories(testFilePath.getParent());
        Files.write(testFilePath, "test content".getBytes());

        when(imageRepository.findById(1L)).thenReturn(Optional.of(testImage));
        doNothing().when(imageRepository).deleteById(1L);

        // Act
        imageService.deleteImage(1L);

        // Assert
        verify(imageRepository, times(1)).deleteById(1L);
        assertFalse(Files.exists(testFilePath));
    }

    @Test
    void deleteImage_WithNonExistingId_ShouldThrowImageNotFoundException() {
        // Arrange
        when(imageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ImageNotFoundException.class, () -> imageService.deleteImage(999L));
    }

    @Test
    void validateImageFile_WithValidImage_ShouldNotThrowException() {
        // Act & Assert (no exception should be thrown)
        assertDoesNotThrow(() -> imageService.validateImageFile(validImageFile));
    }

    @Test
    void validateImageFile_WithInvalidContentType_ShouldThrowException() {
        // Act & Assert
        assertThrows(FileTypeNotSupportedException.class,
                () -> imageService.validateImageFile(invalidImageFile));
    }

    @Test
    void validateImageFile_WithInvalidExtension_ShouldThrowException() {
        // Arrange
        MultipartFile fileWithInvalidExt = new MockMultipartFile(
                "image",
                "test.txt",
                "image/png",  // Correct content type but wrong extension
                "test content".getBytes()
        );

        // Act & Assert
        assertThrows(FileTypeNotSupportedException.class,
                () -> imageService.validateImageFile(fileWithInvalidExt));
    }
}