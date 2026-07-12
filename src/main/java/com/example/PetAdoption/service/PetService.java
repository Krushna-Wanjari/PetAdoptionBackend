package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.PetRequest;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.PetImages;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PetService {

    Pet addPet(PetRequest request);
    Pet getPetById(Long id);
    List<Pet> getAllPets();
    List<Pet> getPetsByStatus(String status);
    List<Pet> getPetsBySpecies(String species);
    Pet updatePet(Long id, PetRequest request);
    void deletePet(Long id);
    Pet updatePetStatus(Long id, String status);

    // --- Cloudinary-backed image management ---
    Pet setMainImage(Long petId, MultipartFile file);
    PetImages addGalleryImage(Long petId, MultipartFile file);
    List<PetImages> getGalleryImages(Long petId);
    void deleteGalleryImage(Long petId, long imageId);
}
