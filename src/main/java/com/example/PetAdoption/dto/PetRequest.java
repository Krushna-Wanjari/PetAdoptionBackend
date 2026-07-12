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
}
