package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.RegisterRequest;
import com.example.PetAdoption.entity.User;
import com.example.PetAdoption.exception.DuplicateResourceException;
import com.example.PetAdoption.exception.ResourceNotFoundException;
import com.example.PetAdoption.repository.AdoptionRequestDao;
import com.example.PetAdoption.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final AdoptionRequestDao adoptionRequestDao;
    private final AdoptionService adoptionService;

    @Autowired
    public UserServiceImpl(UserDao userDao,
                           PasswordEncoder passwordEncoder,
                           AdoptionRequestDao adoptionRequestDao,
                           AdoptionService adoptionService) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.adoptionRequestDao = adoptionRequestDao;
        this.adoptionService = adoptionService;
    }

    @Override
    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userDao.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        userDao.save(user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);

        if (user.getRole() == User.Role.ADMIN && countAdmins() <= 1) {
            throw new IllegalArgumentException("The last remaining admin account cannot be deleted");
        }

        // Remember which pets this user had requested: deleting their requests
        // below can leave those pets stranded in PENDING with nothing left to
        // approve or reject.
        List<Long> affectedPetIds = adoptionRequestDao.findPetIdsByUserId(user.getUserId());

        // adoption_requests.user_id is a foreign key, so clear the history first.
        adoptionRequestDao.deleteByUserId(user.getUserId());

        userDao.delete(id);

        adoptionService.recalculatePetStatuses(affectedPetIds);
    }

    private long countAdmins() {
        return userDao.findAll().stream()
                .filter(u -> u.getRole() == User.Role.ADMIN)
                .count();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }
}
