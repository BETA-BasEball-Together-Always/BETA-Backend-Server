package com.beta.community.infra.storage;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OracleCloudStorageClient {

    private final ObjectStorage objectStorage;

    @Value("${oracle.cloud.storage.namespace}")
    private String namespace;

    @Value("${oracle.cloud.storage.bucket-name}")
    private String bucketName;

    @Value("${oracle.cloud.storage.region}")
    private String region;

    /**
     * 파일 업로드
     * @param inputStream 파일 입력 스트림
     * @param objectName 저장할 객체 이름 (경로 포함)
     * @param contentType MIME 타입
     * @param contentLength 파일 크기
     * @return 업로드된 파일의 공개 URL
     */
    public String upload(InputStream inputStream, String objectName, String contentType, long contentLength) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .putObjectBody(inputStream)
                    .build();

            PutObjectResponse response = objectStorage.putObject(request);

            String publicUrl = buildPublicUrl(objectName);
            log.info("파일 업로드 성공: objectName={}, url={}, eTag={}",
                    objectName, publicUrl, response.getETag());

            return publicUrl;

        } catch (Exception e) {
            log.error("Oracle Cloud Storage 업로드 실패: objectName={}", objectName, e);
            throw new RuntimeException("파일 업로드에 실패했습니다", e);
        }
    }

    /**
     * 공개 URL 생성
     */
    public void delete(String objectName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .build();

            objectStorage.deleteObject(request);
            log.info("파일 삭제 성공: objectName={}", objectName);

        } catch (Exception e) {
            log.error("Oracle Cloud Storage 삭제 실패: objectName={}", objectName, e);
        }
    }

    public void deleteAll(List<String> objectNames) {
        if (objectNames == null || objectNames.isEmpty()) {
            return;
        }

        for (String objectName : objectNames) {
            delete(objectName);
        }
    }

    private String buildPublicUrl(String objectName) {
        return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                region, namespace, bucketName, objectName);
    }
}
