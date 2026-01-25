package com.beta.community.domain.service;

import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.exception.community.ImageUploadException;
import com.beta.core.exception.community.InvalidImageException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostImageServiceTest {

    @InjectMocks
    private PostImageService postImageService;

    @Mock
    private PostImageJpaRepository postImageJpaRepository;

    @Mock
    private OracleCloudStorageClient storageClient;

    @Nested
    @DisplayName("validateImages 실패 케이스")
    class ValidateImagesFailure {

        @Test
        @DisplayName("이미지 개수가 5개 초과하면 예외 발생")
        void throwException_whenImageCountExceedsLimit() {
            List<MultipartFile> images = List.of(
                    mockMultipartFile("image1.jpg", "image/jpeg", 1024),
                    mockMultipartFile("image2.jpg", "image/jpeg", 1024),
                    mockMultipartFile("image3.jpg", "image/jpeg", 1024),
                    mockMultipartFile("image4.jpg", "image/jpeg", 1024),
                    mockMultipartFile("image5.jpg", "image/jpeg", 1024),
                    mockMultipartFile("image6.jpg", "image/jpeg", 1024)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("이미지는 최대 5개까지 업로드 가능합니다");
        }

        @Test
        @DisplayName("파일 크기가 10MB 초과하면 예외 발생")
        void throwException_whenFileSizeExceedsLimit() {
            long oversizedFileSize = 11 * 1024 * 1024;
            List<MultipartFile> images = List.of(
                    mockMultipartFile("large.jpg", "image/jpeg", oversizedFileSize)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("파일 크기는 10MB를 초과할 수 없습니다");
        }

        @Test
        @DisplayName("지원하지 않는 MIME 타입이면 예외 발생")
        void throwException_whenUnsupportedMimeType() {
            List<MultipartFile> images = List.of(
                    mockMultipartFile("document.pdf", "application/pdf", 1024)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("지원하지 않는 이미지 형식입니다");
        }

        @Test
        @DisplayName("MIME 타입이 null이면 예외 발생")
        void throwException_whenMimeTypeIsNull() {
            List<MultipartFile> images = List.of(
                    mockMultipartFile("image.jpg", null, 1024)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("지원하지 않는 이미지 형식입니다");
        }

        @Test
        @DisplayName("파일명이 null이면 예외 발생")
        void throwException_whenFilenameIsNull() {
            List<MultipartFile> images = List.of(
                    mockMultipartFile(null, "image/jpeg", 1024)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("파일명이 유효하지 않습니다");
        }

        @Test
        @DisplayName("파일명이 빈 문자열이면 예외 발생")
        void throwException_whenFilenameIsBlank() {
            List<MultipartFile> images = List.of(
                    mockMultipartFile("  ", "image/jpeg", 1024)
            );

            assertThatThrownBy(() -> postImageService.validateImages(images))
                    .isInstanceOf(InvalidImageException.class)
                    .hasMessageContaining("파일명이 유효하지 않습니다");
        }
    }

    @Nested
    @DisplayName("uploadAndSaveImages 실패 케이스")
    class UploadAndSaveImagesFailure {

        @Test
        @DisplayName("스토리지 업로드 실패 시 예외 발생")
        void throwException_whenStorageUploadFails() throws IOException {
            MultipartFile mockFile = mockMultipartFile("test.jpg", "image/jpeg", 1024);
            given(mockFile.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
            given(storageClient.upload(any(), anyString(), anyString(), anyLong()))
                    .willThrow(new RuntimeException("Storage connection failed"));

            List<MultipartFile> images = List.of(mockFile);

            assertThatThrownBy(() ->
                    postImageService.uploadAndSaveImages(1L, 1L, images))
                    .isInstanceOf(ImageUploadException.class)
                    .hasMessageContaining("이미지 업로드 중 오류가 발생했습니다");
        }

        @Test
        @DisplayName("두 번째 이미지 업로드 실패 시 첫 번째 이미지 삭제 시도")
        void deleteFirstImage_whenSecondImageUploadFails() throws IOException {
            MultipartFile mockFile1 = mockMultipartFile("test1.jpg", "image/jpeg", 1024);
            MultipartFile mockFile2 = mockMultipartFile("test2.jpg", "image/jpeg", 1024);
            given(mockFile1.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));
            given(mockFile2.getInputStream()).willReturn(new ByteArrayInputStream(new byte[0]));

            given(storageClient.upload(any(), anyString(), anyString(), anyLong()))
                    .willReturn("http://storage/test1.jpg")
                    .willThrow(new RuntimeException("Storage connection failed"));

            List<MultipartFile> images = List.of(mockFile1, mockFile2);

            assertThatThrownBy(() ->
                    postImageService.uploadAndSaveImages(1L, 1L, images))
                    .isInstanceOf(ImageUploadException.class);

            verify(storageClient).deleteAll(any());
        }
    }

    private MultipartFile mockMultipartFile(String filename, String contentType, long size) {
        MultipartFile mockFile = mock(MultipartFile.class);
        lenient().when(mockFile.getOriginalFilename()).thenReturn(filename);
        lenient().when(mockFile.getContentType()).thenReturn(contentType);
        lenient().when(mockFile.getSize()).thenReturn(size);
        return mockFile;
    }
}
