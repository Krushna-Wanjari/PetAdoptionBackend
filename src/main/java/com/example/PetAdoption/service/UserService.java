package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.RegisterRequest;
import com.example.PetAdoption.entity.User;

import java.util.List;

public interface UserService {
    User registerUser(RegisterRequest request);
    User getUserById(Long id);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    void deleteUser(Long id);
    boolean existsByEmail(String email);
}
