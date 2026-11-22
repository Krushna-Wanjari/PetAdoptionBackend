package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.Pet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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

    /** Gallery photos, so a listing shows a thumbnail even when the pet has no main photo yet. */
    private List<String> imageUrls;

    private String city;
    private String weight;
    private String color;
    private boolean vaccinated;
    private boolean sterilized;
    private String reasonForRehoming;
    private String ownerName;
    private String ownerPhone;
    private LocalDateTime createdAt;
    private Long createdByUserId;

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
                pet.getImages() == null ? List.of() : pet.getImages().stream()
                        .map(image -> image.getImageUrl())
                        .filter(url -> url != null && !url.isBlank())
                        .toList(),
                pet.getCity(),
                pet.getWeight(),
                pet.getColor(),
                Boolean.TRUE.equals(pet.getVaccinated()),
                Boolean.TRUE.equals(pet.getSterilized()),
                pet.getReasonForRehoming(),
                pet.getOwnerName(),
                pet.getOwnerPhone(),
                pet.getCreatedAt(),
                pet.getCreatedBy() != null ? pet.getCreatedBy().getUserId() : null
        );
    }
}
