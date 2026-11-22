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

    /**
     * user and pet are fetched eagerly on every read path because
     * spring.jpa.open-in-view is disabled: the entities are detached by the time
     * the controller maps them to AdoptionResponseDto, so touching a lazy
     * association there would throw LazyInitializationException.
     */
    @Override
    public List<AdoptionRequest> findAll() {
        return em.createQuery(
                        "select a from AdoptionRequest a "
                                + "left join fetch a.user "
                                + "left join fetch a.pet "
                                + "order by a.createdAt desc",
                        AdoptionRequest.class)
                .getResultList();
    }

    @Override
    public AdoptionRequest findById(long id) {
        List<AdoptionRequest> results = em.createQuery(
                        "select a from AdoptionRequest a "
                                + "left join fetch a.user "
                                + "left join fetch a.pet "
                                + "where a.requestId = :id",
                        AdoptionRequest.class)
                .setParameter("id", id)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<AdoptionRequest> findByUserId(long userId) {
        return em.createQuery(
                        "select a from AdoptionRequest a "
                                + "left join fetch a.user "
                                + "left join fetch a.pet "
                                + "where a.user.userId = :userId "
                                + "order by a.createdAt desc",
                        AdoptionRequest.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<AdoptionRequest> findByPetId(long petId) {
        return em.createQuery(
                        "select a from AdoptionRequest a "
                                + "left join fetch a.user "
                                + "left join fetch a.pet "
                                + "where a.pet.id = :petId "
                                + "order by a.createdAt desc",
                        AdoptionRequest.class)
                .setParameter("petId", petId)
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
                        "select count(a) from AdoptionRequest a "
                                + "where a.pet.id = :petId "
                                + "and a.user.userId = :userId "
                                + "and a.status = :status",
                        Long.class)
                .setParameter("petId", petId)
                .setParameter("userId", userId)
                .setParameter("status", AdoptionRequest.RequestStatus.PENDING)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Override
    public void deleteByPetId(long petId) {
        em.createQuery("delete from AdoptionRequest a where a.pet.id = :petId")
                .setParameter("petId", petId)
                .executeUpdate();
    }

    @Override
    public void deleteByUserId(long userId) {
        em.createQuery("delete from AdoptionRequest a where a.user.userId = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public long countPendingForPet(long petId) {
        Long count = em.createQuery(
                        "select count(a) from AdoptionRequest a "
                                + "where a.pet.id = :petId and a.status = :status",
                        Long.class)
                .setParameter("petId", petId)
                .setParameter("status", AdoptionRequest.RequestStatus.PENDING)
                .getSingleResult();
        return count == null ? 0L : count;
    }

    @Override
    public long countApprovedForPet(long petId) {
        Long count = em.createQuery(
                        "select count(a) from AdoptionRequest a "
                                + "where a.pet.id = :petId and a.status = :status",
                        Long.class)
                .setParameter("petId", petId)
                .setParameter("status", AdoptionRequest.RequestStatus.APPROVED)
                .getSingleResult();
        return count == null ? 0L : count;
    }

    @Override
    public List<Long> findPetIdsByUserId(long userId) {
        return em.createQuery(
                        "select distinct a.pet.id from AdoptionRequest a where a.user.userId = :userId",
                        Long.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
