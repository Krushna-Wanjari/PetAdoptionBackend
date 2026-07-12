package com.example.PetAdoption.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdoptionRequestCreateDto {

    @NotNull(message = "Pet id is required")
    private Long petId;

    @Size(max = 1000, message = "Message must be under 1000 characters")
    private String message;
}
