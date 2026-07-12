package com.example.PetAdoption.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {

    /**
     * Uploads an image to Cloudinary under the "pet-adoption/pets" folder.
     * @return the secure (https) URL Cloudinary returns for the uploaded image
     */
    String uploadPetImage(MultipartFile file);

    /**
     * Deletes an image from Cloudinary given the full secure URL that was returned at upload time.
     */
    void deleteImageByUrl(String imageUrl);
}
