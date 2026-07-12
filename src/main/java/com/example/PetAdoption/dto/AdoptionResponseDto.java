package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.AdoptionRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class AdoptionResponseDto {
    private long requestId;
    private long userId;
    private String userName;
    private long petId;
    private String petName;
    private String status;
    private String message;
    private LocalDateTime createdAt;

    public static AdoptionResponseDto fromEntity(AdoptionRequest req) {
        return new AdoptionResponseDto(
                req.getRequestId(),
                req.getUser().getUserId(),
                req.getUser().getName(),
                req.getPet().getId(),
                req.getPet().getName(),
                req.getStatus().name(),
                req.getMessage(),
                req.getCreatedAt()
        );
    }
}
