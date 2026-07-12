package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.User;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDaoImple implements UserDao {

    private final EntityManager em;

    @Autowired
    public UserDaoImple(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("from User", User.class).getResultList();
    }

    @Override
    public void save(User user) {
        if (user.getUserId() == 0) {
            em.persist(user);
        } else {
            em.merge(user);
        }
    }

    @Override
    public void delete(long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    @Override
    public User findById(long id) {
        return em.find(User.class, id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> results = em.createQuery("from User where email = :email", User.class)
                .setParameter("email", email)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = em.createQuery("select count(u) from User u where u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count != null && count > 0;
    }
}
