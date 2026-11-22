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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.ArrayList;

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

        // Only flip the pet while this is the only request in flight.
        if (adoptionRequestDao.countPendingForPet(pet.getId()) == 1) {
            pet.setStatus(Pet.PetStatus.PENDING);
            petDao.save(pet);
        }

        return request;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequest> getMyRequests(String requesterEmail) {
        User user = userDao.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + requesterEmail));
        return adoptionRequestDao.findByUserId(user.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequest> getReceivedRequests(String ownerEmail, boolean isAdmin) {
        if (isAdmin) {
            return getAllRequests();
        }
        User user = userDao.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + ownerEmail));
        List<AdoptionRequest> all = adoptionRequestDao.findAll();
        List<AdoptionRequest> out = new ArrayList<>();
        for (AdoptionRequest req : all) {
            if (req.getPet() != null && req.getPet().getCreatedBy() != null
                    && req.getPet().getCreatedBy().getUserId() == user.getUserId()) {
                out.add(req);
            }
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdoptionRequest> getAllRequests() {
        return adoptionRequestDao.findAll();
    }

    @Override
    @Transactional
    public AdoptionRequest approveRequest(long requestId) {
        AdoptionRequest request = getPendingRequestOrThrow(requestId);
        request.setStatus(AdoptionRequest.RequestStatus.APPROVED);
        adoptionRequestDao.save(request);

        // Approving one request settles the pet: every other request for it is
        // closed out so the pet cannot stay "pending" on a stale row.
        closeOtherPendingRequests(request);

        Pet pet = request.getPet();
        pet.setStatus(Pet.PetStatus.ADOPTED);
        petDao.save(pet);

        return request;
    }

@Override
    @Transactional
    public AdoptionRequest approveRequest(long requestId, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return approveRequest(requestId);
        }
        AdoptionRequest request = getPendingRequestOrThrow(requestId);
        if (request.getPet() == null || request.getPet().getCreatedBy() == null
                || requesterEmail == null || !requesterEmail.equals(request.getPet().getCreatedBy().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only approve requests for pets you listed");
        }
        return approveRequest(requestId);
    }

    @Override
    @Transactional
    public AdoptionRequest rejectRequest(long requestId) {
        AdoptionRequest request = getPendingRequestOrThrow(requestId);
        request.setStatus(AdoptionRequest.RequestStatus.REJECTED);
        adoptionRequestDao.save(request);

        // Only reopen the pet when nobody else is still waiting on it,
        // otherwise the status would contradict the remaining pending requests.
        Pet pet = request.getPet();
        if (adoptionRequestDao.countPendingForPet(pet.getId()) == 0 && pet.getStatus() == Pet.PetStatus.PENDING) {
            pet.setStatus(Pet.PetStatus.AVAILABLE);
            petDao.save(pet);
        }

        return request;
    }

    @Override
    @Transactional
    public AdoptionRequest rejectRequest(long requestId, String requesterEmail, boolean isAdmin) {
        if (isAdmin) {
            return rejectRequest(requestId);
        }
        AdoptionRequest request = getPendingRequestOrThrow(requestId);
        if (request.getPet() == null || request.getPet().getCreatedBy() == null
                || requesterEmail == null || !requesterEmail.equals(request.getPet().getCreatedBy().getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only reject requests for pets you listed");
        }
        return rejectRequest(requestId);
    }

    private AdoptionRequest getPendingRequestOrThrow(long requestId) {
        AdoptionRequest request = getRequestOrThrow(requestId);
        if (request.getStatus() != AdoptionRequest.RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This request has already been " + request.getStatus().name().toLowerCase());
        }
        return request;
    }

    private void closeOtherPendingRequests(AdoptionRequest approved) {
        for (AdoptionRequest other : adoptionRequestDao.findByPetId(approved.getPet().getId())) {
            if (other.getRequestId() != approved.getRequestId()
                    && other.getStatus() == AdoptionRequest.RequestStatus.PENDING) {
                other.setStatus(AdoptionRequest.RequestStatus.REJECTED);
                adoptionRequestDao.save(other);
            }
        }
    }

    private AdoptionRequest getRequestOrThrow(long requestId) {
        AdoptionRequest request = adoptionRequestDao.findById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Adoption request not found with id: " + requestId);
        }
        return request;
    }

    @Override
    @Transactional
    public void recalculatePetStatuses(List<Long> petIds) {
        for (Long petId : petIds) {
            Pet pet = petDao.findById(petId);
            if (pet == null) {
                continue; // pet was deleted alongside its requests
            }

            boolean hasApproved = adoptionRequestDao.countApprovedForPet(petId) > 0;
            boolean hasPending = adoptionRequestDao.countPendingForPet(petId) > 0;

            if (hasApproved) {
                pet.setStatus(Pet.PetStatus.ADOPTED);
            } else if (hasPending) {
                pet.setStatus(Pet.PetStatus.PENDING);
            } else if (pet.getStatus() == Pet.PetStatus.PENDING) {
                // PENDING was derived from a request that no longer exists.
                // Deliberately conservative: a pet an admin marked ADOPTED by
                // hand keeps that status, since no request drove it.
                pet.setStatus(Pet.PetStatus.AVAILABLE);
            }

            petDao.save(pet);
        }
    }
}
