package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.Pet;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PetDaoImpl implements PetDao {

    private final EntityManager em;

    @Autowired
    public PetDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<Pet> findAll() {
        return em.createQuery("from Pet order by createdAt desc", Pet.class).getResultList();
    }

    @Override
    public Pet findById(long id) {
        return em.find(Pet.class, id);
    }

    @Override
    public void save(Pet pet) {
        if (pet.getId() == 0) {
            em.persist(pet);
        } else {
            em.merge(pet);
        }
    }

    @Override
    public void deleteById(long id) {
        Pet pet = em.find(Pet.class, id);
        if (pet != null) {
            em.remove(pet);
        }
    }

    @Override
    public List<Pet> findByStatus(Pet.PetStatus status) {
        return em.createQuery("from Pet where status = :status order by createdAt desc", Pet.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Pet> findBySpecies(String species) {
        return em.createQuery("from Pet where lower(species) = lower(:species) order by createdAt desc", Pet.class)
                .setParameter("species", species)
                .getResultList();
    }
}
