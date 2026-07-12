package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.PetRequest;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.PetImages;
import com.example.PetAdoption.exception.ResourceNotFoundException;
import com.example.PetAdoption.repository.PetDao;
import com.example.PetAdoption.repository.PetImagesDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetDao petDao;
    private final PetImagesDao petImagesDao;
    private final ImageUploadService imageUploadService;

    @Autowired
    public PetServiceImpl(PetDao petDao, PetImagesDao petImagesDao, ImageUploadService imageUploadService) {
        this.petDao = petDao;
        this.petImagesDao = petImagesDao;
        this.imageUploadService = imageUploadService;
    }

    @Override
    @Transactional
    public Pet addPet(PetRequest request) {
        Pet pet = new Pet();
        applyRequest(pet, request);
        if (pet.getStatus() == null) {
            pet.setStatus(Pet.PetStatus.AVAILABLE);
        }
        petDao.save(pet);
        return pet;
    }

    @Override
    public Pet getPetById(Long id) {
        Pet pet = petDao.findById(id);
        if (pet == null) {
            throw new ResourceNotFoundException("Pet not found with id: " + id);
        }
        return pet;
    }

    @Override
    public List<Pet> getAllPets() {
        return petDao.findAll();
    }

    @Override
    public List<Pet> getPetsByStatus(String status) {
        Pet.PetStatus petStatus;
        try {
            petStatus = Pet.PetStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Use one of: AVAILABLE, ADOPTED, PENDING");
        }
        return petDao.findByStatus(petStatus);
    }

    @Override
    public List<Pet> getPetsBySpecies(String species) {
        return petDao.findBySpecies(species);
    }

    @Override
    @Transactional
    public Pet updatePet(Long id, PetRequest request) {
        Pet pet = getPetById(id);
        applyRequest(pet, request);
        petDao.save(pet);
        return pet;
    }

    @Override
    @Transactional
    public void deletePet(Long id) {
        getPetById(id); // throws if missing
        petDao.deleteById(id);
    }

    @Override
    @Transactional
    public Pet updatePetStatus(Long id, String status) {
        Pet pet = getPetById(id);
        try {
            pet.setStatus(Pet.PetStatus.valueOf(status.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status. Use one of: AVAILABLE, ADOPTED, PENDING");
        }
        petDao.save(pet);
        return pet;
    }

    private void applyRequest(Pet pet, PetRequest request) {
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setAge(request.getAge());
        pet.setGender(request.getGender());
        if (request.getStatus() != null) {
            pet.setStatus(request.getStatus());
        }
        pet.setDescription(request.getDescription());
        pet.setImageUrl(request.getImageUrl());
    }

    @Override
    @Transactional
    public Pet setMainImage(Long petId, MultipartFile file) {
        Pet pet = getPetById(petId);
        String oldUrl = pet.getImageUrl();

        String newUrl = imageUploadService.uploadPetImage(file);
        pet.setImageUrl(newUrl);
        petDao.save(pet);

        // Best-effort cleanup of the previous image so old files don't pile up in Cloudinary.
        if (oldUrl != null && !oldUrl.isBlank()) {
            imageUploadService.deleteImageByUrl(oldUrl);
        }
        return pet;
    }

    @Override
    @Transactional
    public PetImages addGalleryImage(Long petId, MultipartFile file) {
        Pet pet = getPetById(petId);
        String url = imageUploadService.uploadPetImage(file);

        PetImages image = new PetImages(pet, url);
        petImagesDao.save(image);
        return image;
    }

    @Override
    public List<PetImages> getGalleryImages(Long petId) {
        getPetById(petId); // 404 if the pet doesn't exist
        return petImagesDao.findByPetId(petId);
    }

    @Override
    @Transactional
    public void deleteGalleryImage(Long petId, long imageId) {
        getPetById(petId); // 404 if the pet doesn't exist
        PetImages image = petImagesDao.findById(imageId);
        if (image == null || image.getPet().getId() != petId) {
            throw new ResourceNotFoundException("Image not found with id: " + imageId + " for pet: " + petId);
        }
        imageUploadService.deleteImageByUrl(image.getImageUrl());
        petImagesDao.deleteById(imageId);
    }
}
