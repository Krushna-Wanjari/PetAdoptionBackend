package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.PetImages;

import java.util.List;

public interface PetImagesDao {
    void save(PetImages image);
    PetImages findById(long id);
    List<PetImages> findByPetId(long petId);
    void deleteById(long id);
}
