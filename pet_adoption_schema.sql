-- ============================================================
--  Pet Adoption Database Schema
--  Database: MySQL 8+
-- ============================================================

CREATE DATABASE IF NOT EXISTS pet_adoption;
USE pet_adoption;

-- ── 1. USERS ────────────────────────────────────────────────
CREATE TABLE users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,          -- BCrypt hashed
    role        ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
);

-- ── 2. PETS ─────────────────────────────────────────────────
CREATE TABLE pets (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    species     VARCHAR(50)     NOT NULL,          -- Dog / Cat / Bird …
    breed       VARCHAR(100),
    age         INT             NOT NULL,          -- in months
    gender      ENUM('MALE', 'FEMALE') NOT NULL,
    status      ENUM('AVAILABLE', 'ADOPTED', 'PENDING') NOT NULL DEFAULT 'AVAILABLE',
    description TEXT,
    image_url   VARCHAR(500),
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id)
);

-- ── 3. ADOPTION REQUESTS ────────────────────────────────────
CREATE TABLE adoption_requests (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    pet_id      BIGINT          NOT NULL,
    status      ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    message     TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id),
    CONSTRAINT fk_ar_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ar_pet  FOREIGN KEY (pet_id)  REFERENCES pets  (id) ON DELETE CASCADE
);

-- ── 4. PET IMAGES ───────────────────────────────────────────
CREATE TABLE pet_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    pet_id      BIGINT          NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_pi_pet FOREIGN KEY (pet_id) REFERENCES pets (id) ON DELETE CASCADE
);

-- ============================================================
--  Sample Data
-- ============================================================

-- Admin user  (password = "admin123"  BCrypt hashed)
INSERT INTO users (name, email, password, role) VALUES
('Admin User',  'admin@petadoption.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'ADMIN'),
('Krushna Dev', 'krushna@example.com',  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'USER');

-- Sample pets
INSERT INTO pets (name, species, breed, age, gender, status, description, image_url) VALUES
('Biscuit', 'Dog', 'Golden Retriever Mix', 24, 'MALE',   'AVAILABLE', 'Friendly and loves kids.',      'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600'),
('Luna',    'Cat', 'Domestic Shorthair',   36, 'FEMALE', 'AVAILABLE', 'Calm indoor cat.',              'https://images.unsplash.com/photo-1574158622682-e40e69881006?w=600'),
('Mango',   'Dog', 'Labrador Mix',         12, 'MALE',   'AVAILABLE', 'Playful and very energetic.',   'https://images.unsplash.com/photo-1477884213360-7e9d7dcc1e48?w=600'),
('Mochi',   'Cat', 'Persian Mix',          48, 'FEMALE', 'PENDING',   'Quiet and loves cuddles.',      'https://images.unsplash.com/photo-1495360010541-f48722b34f7d?w=600'),
('Clover',  'Rabbit', 'Holland Lop',       12, 'FEMALE', 'AVAILABLE', 'Gentle and quiet.',             'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?w=600'),
('Duke',    'Dog', 'Border Collie Mix',    60, 'MALE',   'AVAILABLE', 'Smart and loves outdoor runs.', 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=600');

-- Sample adoption request
INSERT INTO adoption_requests (user_id, pet_id, status, message) VALUES
(2, 1, 'PENDING', 'I have a big yard and would love to give Biscuit a forever home!');

-- Sample extra images for Biscuit
INSERT INTO pet_images (pet_id, image_url) VALUES
(1, 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=600'),
(1, 'https://images.unsplash.com/photo-1552053831-71594a27632d?w=600');
