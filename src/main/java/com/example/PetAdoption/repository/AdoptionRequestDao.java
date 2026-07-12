package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.AdoptionRequest;

import java.util.List;

public interface AdoptionRequestDao {

    List<AdoptionRequest> findAll();
    AdoptionRequest findById(long id);
    List<AdoptionRequest> findByUserId(long userId);
    void save(AdoptionRequest request);
    boolean existsPendingByUserAndPet(long userId, long petId);
}
