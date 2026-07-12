package com.example.PetAdoption.config;

import com.example.PetAdoption.entity.User;
import com.example.PetAdoption.repository.UserDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a default ADMIN account on startup so the API can be exercised
 * via Swagger immediately, without needing to manually promote a user.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@petadoption.com}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    public DataSeeder(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userDao.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            userDao.save(admin);
            System.out.println("Seeded default admin -> email: " + adminEmail + " | password: " + adminPassword);
        }
    }
}
