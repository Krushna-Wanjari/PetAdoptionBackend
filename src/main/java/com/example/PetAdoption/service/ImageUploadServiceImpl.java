package com.example.PetAdoption.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {

    private static final String FOLDER = "pet-adoption/pets";

    // Matches ".../upload/v12345/pet-adoption/pets/abcdef.jpg" -> captures "pet-adoption/pets/abcdef"
    private static final Pattern PUBLIC_ID_PATTERN =
            Pattern.compile("/upload/(?:v\\d+/)?(.+)\\.[a-zA-Z0-9]+$");

    private final Cloudinary cloudinary;

    @Autowired
    public ImageUploadServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadPetImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file was provided");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", FOLDER,
                    "resource_type", "image",
                    "overwrite", true
            ));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null) {
                throw new IllegalStateException("Cloudinary did not return a URL for the uploaded image");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImageByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String publicId = extractPublicId(imageUrl);
        if (publicId == null) {
            return; // not a Cloudinary URL we recognize - nothing to clean up
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
        } catch (IOException e) {
            // Deletion failures shouldn't block the main operation (e.g. updating/deleting a pet);
            // log and move on rather than throwing.
            System.err.println("Warning: failed to delete Cloudinary image [" + publicId + "]: " + e.getMessage());
        }
    }

    private String extractPublicId(String secureUrl) {
        Matcher matcher = PUBLIC_ID_PATTERN.matcher(secureUrl);
        return matcher.find() ? matcher.group(1) : null;
    }
}
