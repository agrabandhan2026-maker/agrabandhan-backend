package com.agrabandhan.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final S3Client s3Client;

    @Value("${agrabandhan.r2.bucket-name:agrabandhan-media}")
    private String bucketName;

    @Value("${agrabandhan.r2.public-url:}")
    private String publicUrl;

    private static final int THUMBNAIL_SIZE = 150;
    private static final int MEDIUM_SIZE = 400;

    /**
     * Upload a profile photo with 3 sizes: original, medium (400px), thumbnail (150px).
     * Returns a map with keys: original, medium, thumbnail containing the R2 object keys.
     */
    public Map<String, String> uploadProfilePhoto(Long userId, MultipartFile file) throws IOException {
        validateImage(file);

        String fileExtension = getExtension(file.getOriginalFilename());
        String baseKey = "profiles/" + userId + "/photos/" + UUID.randomUUID();

        String originalKey = baseKey + "/original." + fileExtension;
        String mediumKey = baseKey + "/medium." + fileExtension;
        String thumbnailKey = baseKey + "/thumbnail." + fileExtension;

        byte[] originalBytes = file.getBytes();
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalBytes));

        if (originalImage == null) {
            throw new IOException("Unable to read image file. Ensure it is a valid image.");
        }

        // Upload original
        uploadToR2(originalKey, originalBytes, file.getContentType());

        // Resize and upload medium
        byte[] mediumBytes = resizeImage(originalImage, MEDIUM_SIZE, fileExtension);
        uploadToR2(mediumKey, mediumBytes, file.getContentType());

        // Resize and upload thumbnail
        byte[] thumbnailBytes = resizeImage(originalImage, THUMBNAIL_SIZE, fileExtension);
        uploadToR2(thumbnailKey, thumbnailBytes, file.getContentType());

        log.info("Uploaded 3 photo variants for userId: {}, key: {}", userId, baseKey);

        return Map.of(
                "original", originalKey,
                "medium", mediumKey,
                "thumbnail", thumbnailKey
        );
    }

    /**
     * Upload a voice introduction audio file.
     */
    public String uploadVoiceIntro(Long userId, MultipartFile file) throws IOException {
        validateAudio(file);

        String fileExtension = getExtension(file.getOriginalFilename());
        String key = "profiles/" + userId + "/voice/" + UUID.randomUUID() + "." + fileExtension;

        uploadToR2(key, file.getBytes(), file.getContentType());
        log.info("Uploaded voice intro for userId: {}", userId);

        return key;
    }

    /**
     * Delete a file from R2.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Deleted file from R2: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from R2: {}", key, e);
        }
    }

    /**
     * Get the full public URL for a given R2 object key.
     */
    public String getPublicUrl(String key) {
        if (key == null || key.isBlank()) return null;
        return publicUrl + "/" + key;
    }

    public String getPublicBaseUrl() {
        return publicUrl;
    }

    // =============================================
    //  Private helpers
    // =============================================

    private void uploadToR2(String key, byte[] data, String contentType) {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .contentType(contentType)
                        .contentLength((long) data.length)
                        .build(),
                RequestBody.fromBytes(data)
        );
    }

    private byte[] resizeImage(BufferedImage original, int targetSize, String format) throws IOException {
        int width = original.getWidth();
        int height = original.getHeight();

        // Calculate proportional dimensions
        double ratio = (double) targetSize / Math.max(width, height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        // Don't upscale
        if (ratio >= 1.0) {
            newWidth = width;
            newHeight = height;
        }

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String imageFormat = format.equalsIgnoreCase("png") ? "png" : "jpg";
        ImageIO.write(resized, imageFormat, baos);

        return baos.toByteArray();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed (JPEG, PNG, WebP)");
        }

        // Max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size must be under 5MB");
        }
    }

    private void validateAudio(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("audio/")) {
            throw new IllegalArgumentException("Only audio files are allowed");
        }

        // Max 5MB (~60 seconds at reasonable quality)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Audio file must be under 5MB");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
