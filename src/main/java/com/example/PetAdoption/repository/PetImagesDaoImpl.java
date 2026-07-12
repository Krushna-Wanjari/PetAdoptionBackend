package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.PetImages;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PetImagesDaoImpl implements PetImagesDao {

    private final EntityManager em;

    @Autowired
    public PetImagesDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(PetImages image) {
        if (image.getId() == 0) {
            em.persist(image);
        } else {
            em.merge(image);
        }
    }

    @Override
    public PetImages findById(long id) {
        return em.find(PetImages.class, id);
    }

    @Override
    public List<PetImages> findByPetId(long petId) {
        return em.createQuery("from PetImages where pet.id = :petId", PetImages.class)
                .setParameter("petId", petId)
                .getResultList();
    }

    @Override
    public void deleteById(long id) {
        PetImages image = em.find(PetImages.class, id);
        if (image != null) {
            em.remove(image);
        }
    }
}
