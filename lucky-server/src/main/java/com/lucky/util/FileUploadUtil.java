package com.lucky.util;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

public class FileUploadUtil {

    private static final String UPLOAD_DIR = "uploads";
    private static final Set<String> IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final Set<String> VIDEO_TYPES = Set.of("video/mp4", "video/webm", "video/ogg");

    public static boolean isImage(MultipartFile file) {
        return file.getContentType() != null && IMAGE_TYPES.contains(file.getContentType());
    }

    public static boolean isVideo(MultipartFile file) {
        return file.getContentType() != null && VIDEO_TYPES.contains(file.getContentType());
    }

    public static String upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());
        return "/uploads/" + newFilename;
    }

    public static void delete(String url) {
        if (url != null && url.startsWith("/uploads/")) {
            try {
                Files.deleteIfExists(Paths.get(url.substring(1)));
            } catch (IOException e) {
            }
        }
    }
}
