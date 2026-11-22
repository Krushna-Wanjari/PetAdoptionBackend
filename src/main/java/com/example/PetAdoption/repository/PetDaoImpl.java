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

    /**
     * Every read fetches the gallery eagerly: spring.jpa.open-in-view is off, and
     * PetResponse.fromEntity reads pet.getImages() after the service transaction
     * has closed, which would otherwise throw LazyInitializationException.
     */
    @Override
    public List<Pet> findAll() {
        return em.createQuery("select distinct p from Pet p left join fetch p.images order by p.createdAt desc", Pet.class)
                .getResultList();
    }

    @Override
    public Pet findById(long id) {
        List<Pet> results = em.createQuery(
                        "select distinct p from Pet p left join fetch p.images where p.id = :id", Pet.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
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
        return em.createQuery(
                        "select distinct p from Pet p left join fetch p.images where p.status = :status order by p.createdAt desc",
                        Pet.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public List<Pet> findBySpecies(String species) {
        return em.createQuery(
                        "select distinct p from Pet p left join fetch p.images "
                                + "where lower(p.species) = lower(:species) order by p.createdAt desc",
                        Pet.class)
                .setParameter("species", species)
                .getResultList();
    }

    @Override
    public List<Pet> search(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return em.createQuery(
                        "select distinct p from Pet p left join fetch p.images "
                                + "where lower(p.name) like :p or lower(p.species) like :p "
                                + "or lower(p.breed) like :p or lower(p.description) like :p "
                                + "or lower(p.city) like :p or lower(p.color) like :p "
                                + "order by p.createdAt desc",
                        Pet.class)
                .setParameter("p", pattern)
                .getResultList();
    }

    @Override
    public List<Pet> findByCreatorId(long userId) {
        return em.createQuery(
                        "select distinct p from Pet p left join fetch p.images "
                                + "where p.createdBy.userId = :userId "
                                + "order by p.createdAt desc",
                        Pet.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
