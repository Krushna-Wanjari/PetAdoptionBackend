package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.PetRequest;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.PetImages;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PetService {

    Pet addPet(PetRequest request);
    Pet addPet(PetRequest request, String creatorEmail);
    Pet getPetById(Long id);
    List<Pet> getAllPets();
    List<Pet> getPetsByStatus(String status);
    List<Pet> getPetsBySpecies(String species);
    List<Pet> searchPets(String query);
    List<Pet> getPetsByCreator(String creatorEmail);
    Pet updatePet(Long id, PetRequest request);
    Pet updatePet(Long id, PetRequest request, String requesterEmail, boolean isAdmin);
    void deletePet(Long id);
    void deletePet(Long id, String requesterEmail, boolean isAdmin);
    Pet updatePetStatus(Long id, String status);

    // --- Cloudinary-backed image management ---
    Pet setMainImage(Long petId, MultipartFile file);
    Pet setMainImage(Long petId, MultipartFile file, String requesterEmail, boolean isAdmin);
    PetImages addGalleryImage(Long petId, MultipartFile file);
    PetImages addGalleryImage(Long petId, MultipartFile file, String requesterEmail, boolean isAdmin);
    List<PetImages> getGalleryImages(Long petId);
    void deleteGalleryImage(Long petId, long imageId);
    void deleteGalleryImage(Long petId, long imageId, String requesterEmail, boolean isAdmin);
}
