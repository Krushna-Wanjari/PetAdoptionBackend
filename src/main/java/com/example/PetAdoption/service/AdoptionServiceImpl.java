package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.AdoptionRequestCreateDto;
import com.example.PetAdoption.entity.AdoptionRequest;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.User;
import com.example.PetAdoption.exception.DuplicateResourceException;
import com.example.PetAdoption.exception.ResourceNotFoundException;
import com.example.PetAdoption.repository.AdoptionRequestDao;
import com.example.PetAdoption.repository.PetDao;
import com.example.PetAdoption.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdoptionServiceImpl implements AdoptionService {

    private final AdoptionRequestDao adoptionRequestDao;
    private final PetDao petDao;
    private final UserDao userDao;

    @Autowired
    public AdoptionServiceImpl(AdoptionRequestDao adoptionRequestDao, PetDao petDao, UserDao userDao) {
        this.adoptionRequestDao = adoptionRequestDao;
        this.petDao = petDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public AdoptionRequest createRequest(String requesterEmail, AdoptionRequestCreateDto dto) {
        User user = userDao.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterEmail));

        Pet pet = petDao.findById(dto.getPetId());
        if (pet == null) {
            throw new ResourceNotFoundException("Pet not found with id: " + dto.getPetId());
        }
        if (pet.getStatus() == Pet.PetStatus.ADOPTED) {
            throw new IllegalArgumentException("This pet has already been adopted");
        }
        if (adoptionRequestDao.existsPendingByUserAndPet(user.getUserId(), pet.getId())) {
            throw new DuplicateResourceException("You already have a pending request for this pet");
        }

        AdoptionRequest request = new AdoptionRequest();
        request.setUser(user);
        request.setPet(pet);
        request.setMessage(dto.getMessage());
        request.setStatus(AdoptionRequest.RequestStatus.PENDING);
        adoptionRequestDao.save(request);

        pet.setStatus(Pet.PetStatus.PENDING);
        petDao.save(pet);

        return request;
    }

    @Override
    public List<AdoptionRequest> getMyRequests(String requesterEmail) {
        User user = userDao.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterEmail));
        return adoptionRequestDao.findByUserId(user.getUserId());
    }

    @Override
    public List<AdoptionRequest> getAllRequests() {
        return adoptionRequestDao.findAll();
    }

    @Override
    @Transactional
    public AdoptionRequest approveRequest(long requestId) {
        AdoptionRequest request = getRequestOrThrow(requestId);
        request.setStatus(AdoptionRequest.RequestStatus.APPROVED);
        adoptionRequestDao.save(request);

        Pet pet = request.getPet();
        pet.setStatus(Pet.PetStatus.ADOPTED);
        petDao.save(pet);

        return request;
    }

    @Override
    @Transactional
    public AdoptionRequest rejectRequest(long requestId) {
        AdoptionRequest request = getRequestOrThrow(requestId);
        request.setStatus(AdoptionRequest.RequestStatus.REJECTED);
        adoptionRequestDao.save(request);

        Pet pet = request.getPet();
        if (pet.getStatus() == Pet.PetStatus.PENDING) {
            pet.setStatus(Pet.PetStatus.AVAILABLE);
            petDao.save(pet);
        }

        return request;
    }

    private AdoptionRequest getRequestOrThrow(long requestId) {
        AdoptionRequest request = adoptionRequestDao.findById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Adoption request not found with id: " + requestId);
        }
        return request;
    }
}
