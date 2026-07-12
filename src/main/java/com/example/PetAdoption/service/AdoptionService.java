package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.AdoptionRequestCreateDto;
import com.example.PetAdoption.entity.AdoptionRequest;

import java.util.List;

public interface AdoptionService {
    AdoptionRequest createRequest(String requesterEmail, AdoptionRequestCreateDto dto);
    List<AdoptionRequest> getMyRequests(String requesterEmail);
    List<AdoptionRequest> getAllRequests();
    AdoptionRequest approveRequest(long requestId);
    AdoptionRequest rejectRequest(long requestId);
}
