package com.example.PetAdoption.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "pet_images")
public class PetImages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotNull(message = "Pet is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Pet pet;

    @NotNull(message = "Image URL is required")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public PetImages(Pet pet, String imageUrl) {
        this.pet = pet;
        this.imageUrl = imageUrl;
    }
}
