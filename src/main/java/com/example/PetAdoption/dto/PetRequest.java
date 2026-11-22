package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.Pet;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Species is required")
    @Size(max = 50)
    private String species;

    @Size(max = 100)
    private String breed;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 50, message = "Age must be realistic")
    private int age;

    @NotNull(message = "Gender is required")
    private Pet.Gender gender;

    private Pet.PetStatus status;

    @Size(max = 2000, message = "Description must be under 2000 characters")
    private String description;

    private String imageUrl;

    @Size(max = 100, message = "City must be under 100 characters")
    private String city;

    @Size(max = 50, message = "Weight must be under 50 characters")
    private String weight;

    @Size(max = 50, message = "Color must be under 50 characters")
    private String color;

    private Boolean vaccinated;

    private Boolean sterilized;

    @Size(max = 2000, message = "Reason must be under 2000 characters")
    private String reasonForRehoming;

    @Size(max = 100, message = "Owner name must be under 100 characters")
    private String ownerName;

    @Size(max = 30, message = "Owner phone must be under 30 characters")
    private String ownerPhone;
}
