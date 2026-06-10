package com.lucky.service;

import com.aliyun.oss.OSS;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssService {

    private final OSS ossClient;

    @Value("${alibaba.oss.bucket-name}")
    private String bucketName;

    @Value("${alibaba.oss.endpoint}")
    private String endpoint;

    public String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = "lucky/background/" + UUID.randomUUID() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            ossClient.putObject(bucketName, objectName, inputStream);
        }

        return "https://" + bucketName + "." + endpoint + "/" + objectName;
    }

    public void delete(String url) {
        if (url == null || url.isEmpty()) {
            return;
        }
        try {
            String prefix = "https://" + bucketName + "." + endpoint + "/";
            if (url.startsWith(prefix)) {
                String objectName = url.substring(prefix.length());
                ossClient.deleteObject(bucketName, objectName);
            }
        } catch (Exception e) {
            // ignore
        }
    }
}