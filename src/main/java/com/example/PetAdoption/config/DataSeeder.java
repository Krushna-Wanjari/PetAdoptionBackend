package com.example.PetAdoption.config;

import com.example.PetAdoption.entity.User;
import com.example.PetAdoption.repository.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds a default ADMIN account on startup so the API can be exercised
 * via Swagger immediately, without needing to manually promote a user.
 *
 * The seed is idempotent: the account is created only if the email is free.
 * When the account already exists the role is still re-asserted, and the
 * password is re-synced only if {@code app.seed.admin-sync-password} is true
 * (enabled in the local .env) - that keeps local development reproducible
 * without silently resetting an administrator's password on every restart
 * in a real deployment.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin-email:admin@petadoption.com}")
    private String adminEmail;

    @Value("${app.seed.admin-password:Admin@123}")
    private String adminPassword;

    @Value("${app.seed.admin-sync-password:false}")
    private boolean syncPassword;

    public DataSeeder(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        User admin = userDao.findByEmail(adminEmail).orElse(null);

        if (admin == null) {
            admin = new User();
            admin.setName("Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            userDao.save(admin);
            // Deliberately no password in the log: application logs are commonly
            // shipped to aggregators, and this value is a real credential.
            log.info("Seeded default admin -> email: {} (password taken from app.seed.admin-password)", adminEmail);
            return;
        }

        boolean changed = false;

        if (admin.getRole() != User.Role.ADMIN) {
            log.warn("Promoting existing user '{}' to ADMIN (seeded admin email)", adminEmail);
            admin.setRole(User.Role.ADMIN);
            changed = true;
        }

        if (syncPassword && !passwordEncoder.matches(adminPassword, admin.getPassword())) {
            log.warn("Re-syncing the password for seeded admin '{}' (app.seed.admin-sync-password=true)", adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            changed = true;
        }

        if (changed) {
            userDao.save(admin);
        }

        if (!syncPassword) {
            log.info("Seeded admin '{}' already exists; password left untouched. "
                    + "Set app.seed.admin-sync-password=true to force it to match app.seed.admin-password.", adminEmail);
        }
    }
}
