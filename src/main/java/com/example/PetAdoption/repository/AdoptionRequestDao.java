package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.AdoptionRequest;

import java.util.List;

public interface AdoptionRequestDao {

    List<AdoptionRequest> findAll();
    AdoptionRequest findById(long id);
    List<AdoptionRequest> findByUserId(long userId);
    List<AdoptionRequest> findByPetId(long petId);
    void save(AdoptionRequest request);
    boolean existsPendingByUserAndPet(long userId, long petId);

    /** Number of PENDING requests currently attached to a pet. */
    long countPendingForPet(long petId);

    /** Number of APPROVED requests currently attached to a pet. */
    long countApprovedForPet(long petId);

    /**
     * Ids of the pets a user has ever requested, so their statuses can be
     * recomputed after the requests themselves are deleted.
     */
    List<Long> findPetIdsByUserId(long userId);

    /** Removes a pet's adoption history, so the pet row can be deleted. */
    void deleteByPetId(long petId);

    /** Removes a user's adoption history, so the user row can be deleted. */
    void deleteByUserId(long userId);
}
