package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PetResponse {
    private long id;
    private String name;
    private String species;
    private String breed;
    private int age;
    private String gender;
    private String status;
    private String description;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static PetResponse fromEntity(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getAge(),
                pet.getGender() != null ? pet.getGender().name() : null,
                pet.getStatus() != null ? pet.getStatus().name() : null,
                pet.getDescription(),
                pet.getImageUrl(),
                pet.getCreatedAt()
        );
    }
}
