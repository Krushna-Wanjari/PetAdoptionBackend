package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.PetImages;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PetImageResponse {
    private long id;
    private long petId;
    private String imageUrl;

    public static PetImageResponse fromEntity(PetImages image) {
        return new PetImageResponse(image.getId(), image.getPet().getId(), image.getImageUrl());
    }
}
