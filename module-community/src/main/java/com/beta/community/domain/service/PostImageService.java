package com.beta.community.domain.service;


import com.beta.community.domain.entity.PostImage;
import com.beta.community.domain.entity.Status;
import com.beta.community.infra.repository.PostImageJpaRepository;
import com.beta.community.infra.storage.OracleCloudStorageClient;
import com.beta.core.exception.community.ImageUploadException;
import com.beta.core.exception.community.InvalidImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostImageService {

    private static final int MAX_IMAGE_COUNT = 5;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_MIME_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final PostImageJpaRepository postImageJpaRepository;
    private final OracleCloudStorageClient storageClient;

    public void validateImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new InvalidImageException(
                    String.format("이미지는 최대 %d개까지 업로드 가능합니다", MAX_IMAGE_COUNT)
            );
        }

        for (MultipartFile image : images) {
            validateImage(image);
        }
    }

    private void validateImage(MultipartFile image) {
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageException(
                    String.format("파일 크기는 %dMB를 초과할 수 없습니다", MAX_FILE_SIZE / 1024 / 1024)
            );
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageException(
                    String.format("지원하지 않는 이미지 형식입니다: %s", contentType)
            );
        }

        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidImageException("파일명이 유효하지 않습니다");
        }
    }

    @Transactional
    public List<String> uploadAndSaveImages(Long postId, Long userId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<String> imageUrls = new ArrayList<>();
        List<String> uploadedObjectNames = new ArrayList<>();

        try {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);

                String originalFilename = image.getOriginalFilename();
                String extension = extractExtension(originalFilename == null ? "" : originalFilename);
                String objectName = generateFilename(userId, i, extension);

                String imageUrl = storageClient.upload(
                        image.getInputStream(),
                        objectName,
                        image.getContentType(),
                        image.getSize()
                );

                uploadedObjectNames.add(objectName);

                PostImage postImage = PostImage.builder()
                        .postId(postId)
                        .userId(userId)
                        .imgUrl(imageUrl)
                        .originName(originalFilename)
                        .newName(objectName)
                        .sort(i)
                        .fileSize(image.getSize())
                        .mimeType(image.getContentType())
                        .status(Status.ACTIVE)
                        .build();

                postImageJpaRepository.save(postImage);
                imageUrls.add(imageUrl);
            }

            return imageUrls;

        } catch (Exception e) {
            log.error("이미지 업로드/저장 실패, 보상 트랜잭션 시작: postId={}, uploadedCount={}",
                    postId, uploadedObjectNames.size(), e);
            deleteUploadedFilesAsync(uploadedObjectNames);
            throw new ImageUploadException(
                    "이미지 업로드 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    @Async("fileCleanupExecutor")
    public void deleteUploadedFilesAsync(List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }

        try {
            storageClient.deleteAll(objectNames);
        } catch (Exception e) {
            log.error("비동기 파일 삭제 실패 (고아 파일 발생 가능): count={}, files={}",
                    objectNames.size(), objectNames, e);
        }
    }

    private String extractExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return ".jpg";
    }

    private String generateFilename(Long userId, int index, String extension) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = String.format("BETA_%s_%d_%03d%s", dateStr, userId, index + 1, extension);
        return String.format("%s/%s", dateStr, filename);
    }
}
