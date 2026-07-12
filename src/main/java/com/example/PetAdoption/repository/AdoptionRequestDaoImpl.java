package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.AdoptionRequest;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdoptionRequestDaoImpl implements AdoptionRequestDao {

    private final EntityManager em;

    @Autowired
    public AdoptionRequestDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<AdoptionRequest> findAll() {
        return em.createQuery("from AdoptionRequest order by createdAt desc", AdoptionRequest.class)
                .getResultList();
    }

    @Override
    public AdoptionRequest findById(long id) {
        return em.find(AdoptionRequest.class, id);
    }

    @Override
    public List<AdoptionRequest> findByUserId(long userId) {
        return em.createQuery("from AdoptionRequest where user.userId = :userId order by createdAt desc", AdoptionRequest.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void save(AdoptionRequest request) {
        if (request.getRequestId() == 0) {
            em.persist(request);
        } else {
            em.merge(request);
        }
    }

    @Override
    public boolean existsPendingByUserAndPet(long userId, long petId) {
        Long count = em.createQuery(
                        "select count(a) from AdoptionRequest a where a.user.userId = :userId and a.pet.id = :petId and a.status = :status",
                        Long.class)
                .setParameter("userId", userId)
                .setParameter("petId", petId)
                .setParameter("status", AdoptionRequest.RequestStatus.PENDING)
                .getSingleResult();
        return count != null && count > 0;
    }
}
