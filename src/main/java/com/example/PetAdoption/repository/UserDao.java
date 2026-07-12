package com.example.PetAdoption.repository;

import com.example.PetAdoption.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {

    List<User> findAll();
    void save(User user);
    void delete(long id);
    User findById(long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
