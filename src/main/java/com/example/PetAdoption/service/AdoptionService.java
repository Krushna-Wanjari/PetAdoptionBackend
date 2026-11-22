package com.example.PetAdoption.service;

import com.example.PetAdoption.dto.AdoptionRequestCreateDto;
import com.example.PetAdoption.entity.AdoptionRequest;

import java.util.List;

public interface AdoptionService {
    AdoptionRequest createRequest(String requesterEmail, AdoptionRequestCreateDto dto);
    List<AdoptionRequest> getMyRequests(String requesterEmail);
    List<AdoptionRequest> getReceivedRequests(String ownerEmail, boolean isAdmin);
    List<AdoptionRequest> getAllRequests();
    AdoptionRequest approveRequest(long requestId);
    AdoptionRequest approveRequest(long requestId, String requesterEmail, boolean isAdmin);
    AdoptionRequest rejectRequest(long requestId);
    AdoptionRequest rejectRequest(long requestId, String requesterEmail, boolean isAdmin);

    /**
     * Re-derives each supplied pet's status from its remaining requests.
     * Needed when requests disappear for a reason other than a decision being
     * made on them - e.g. the requesting user account is deleted - otherwise a
     * pet can be stranded in PENDING with nothing left to approve or reject.
     */
    void recalculatePetStatuses(List<Long> petIds);
}
