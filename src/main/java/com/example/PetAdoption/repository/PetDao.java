package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.Pet;

import java.util.List;

public interface PetDao {

    List<Pet> findAll();
    Pet findById(long id);
    void save(Pet pet);
    void deleteById(long id);
    List<Pet> findByStatus(Pet.PetStatus status);
    List<Pet> findBySpecies(String species);
}
