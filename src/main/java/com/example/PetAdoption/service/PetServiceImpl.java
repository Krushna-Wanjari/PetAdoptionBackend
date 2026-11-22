package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.PetRequest;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.PetImages;
import com.example.PetAdoption.exception.ResourceNotFoundException;
import com.example.PetAdoption.repository.AdoptionRequestDao;
import com.example.PetAdoption.repository.PetDao;
import com.example.PetAdoption.repository.PetImagesDao;
import com.example.PetAdoption.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetDao petDao;
    private final PetImagesDao petImagesDao;
    private final ImageUploadService imageUploadService;
    private final AdoptionRequestDao adoptionRequestDao;
    private final UserDao userDao;

    @Autowired
    public PetServiceImpl(PetDao petDao, PetImagesDao petImagesDao, ImageUploadService imageUploadService,
                          AdoptionRequestDao adoptionRequestDao, UserDao userDao) {
        this.petDao = petDao;
        this.petImagesDao = petImagesDao;
        this.imageUploadService = imageUploadService;
        this.adoptionRequestDao = adoptionRequestDao;
        this.userDao = userDao;
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
    @Transactional
    public Pet addPet(PetRequest request, String creatorEmail) {
        Pet pet = new Pet();
        if (creatorEmail != null && !creatorEmail.isBlank()) {
            userDao.findByEmail(creatorEmail).ifPresent(pet::setCreatedBy);
        }
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
    public List<Pet> searchPets(String query) {
        return petDao.search(query);
    }

    @Override
    public List<Pet> getPetsByCreator(String creatorEmail) {
        if (creatorEmail == null || creatorEmail.isBlank()) {
            return List.of();
        }
        return userDao.findByEmail(creatorEmail)
                .map(user -> petDao.findByCreatorId(user.getUserId()))
                .orElse(List.of());
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
    public Pet updatePet(Long id, PetRequest request, String requesterEmail, boolean isAdmin) {
        Pet pet = getPetById(id);
        if (!isAdmin) {
            if (requesterEmail == null || requesterEmail.isBlank() || pet.getCreatedBy() == null
                    || !requesterEmail.equals(pet.getCreatedBy().getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only edit pets you listed");
            }
            request.setStatus(null);
        }
        applyRequest(pet, request);
        petDao.save(pet);
        return pet;
    }

    @Override
    @Transactional
    public void deletePet(Long id) {
        Pet pet = getPetById(id);
        adoptionRequestDao.deleteByPetId(pet.getId());
        imageUploadService.deleteImageByUrl(pet.getImageUrl());
        for (PetImages image : petImagesDao.findByPetId(pet.getId())) {
            imageUploadService.deleteImageByUrl(image.getImageUrl());
        }
        petDao.deleteById(id);
    }

    @Override
    @Transactional
    public void deletePet(Long id, String requesterEmail, boolean isAdmin) {
        if (!isAdmin) {
            Pet pet = getPetById(id);
            if (requesterEmail == null || requesterEmail.isBlank() || pet.getCreatedBy() == null
                    || !requesterEmail.equals(pet.getCreatedBy().getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete pets you listed");
            }
        }
        deletePet(id);
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
        pet.setCity(request.getCity());
        pet.setWeight(request.getWeight());
        pet.setColor(request.getColor());
        pet.setReasonForRehoming(request.getReasonForRehoming());
        pet.setOwnerName(request.getOwnerName());
        pet.setOwnerPhone(request.getOwnerPhone());

        if (request.getVaccinated() != null) {
            pet.setVaccinated(request.getVaccinated());
        } else if (pet.getId() == 0) {
            pet.setVaccinated(false);
        }
        if (request.getSterilized() != null) {
            pet.setSterilized(request.getSterilized());
        } else if (pet.getId() == 0) {
            pet.setSterilized(false);
        }

        if (request.getImageUrl() != null) {
            pet.setImageUrl(request.getImageUrl());
        }
    }

    @Override
    @Transactional
    public Pet setMainImage(Long petId, MultipartFile file) {
        Pet pet = getPetById(petId);
        String oldUrl = pet.getImageUrl();
        String newUrl = imageUploadService.uploadPetImage(file);
        pet.setImageUrl(newUrl);
        petDao.save(pet);
        if (oldUrl != null && !oldUrl.isBlank()) {
            imageUploadService.deleteImageByUrl(oldUrl);
        }
        return pet;
    }

    @Override
    @Transactional
    public Pet setMainImage(Long petId, MultipartFile file, String requesterEmail, boolean isAdmin) {
        checkCanManageImages(getPetById(petId), requesterEmail, isAdmin);
        return setMainImage(petId, file);
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
    @Transactional
    public PetImages addGalleryImage(Long petId, MultipartFile file, String requesterEmail, boolean isAdmin) {
        checkCanManageImages(getPetById(petId), requesterEmail, isAdmin);
        return addGalleryImage(petId, file);
    }

    @Override
    public List<PetImages> getGalleryImages(Long petId) {
        getPetById(petId);
        return petImagesDao.findByPetId(petId);
    }

    @Override
    @Transactional
    public void deleteGalleryImage(Long petId, long imageId) {
        getPetById(petId);
        PetImages image = petImagesDao.findById(imageId);
        if (image == null || image.getPet().getId() != petId) {
            throw new ResourceNotFoundException("Image not found with id: " + imageId + " for pet: " + petId);
        }
        imageUploadService.deleteImageByUrl(image.getImageUrl());
        petImagesDao.deleteById(imageId);
    }

    @Override
    @Transactional
    public void deleteGalleryImage(Long petId, long imageId, String requesterEmail, boolean isAdmin) {
        if (!isAdmin) {
            Pet pet = getPetById(petId);
            if (requesterEmail == null || requesterEmail.isBlank() || pet.getCreatedBy() == null
                    || !requesterEmail.equals(pet.getCreatedBy().getEmail())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage images for pets you listed");
            }
        }
        deleteGalleryImage(petId, imageId);
    }

    private void checkCanManageImages(Pet pet, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (requesterEmail == null || requesterEmail.isBlank() || pet.getCreatedBy() == null
                || !requesterEmail.equals(pet.getCreatedBy().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only manage images for pets you listed");
        }
    }
}
