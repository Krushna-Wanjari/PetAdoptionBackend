package com.example.PetAdoption.controller;

import com.example.PetAdoption.dto.AdoptionRequestCreateDto;
import com.example.PetAdoption.dto.AdoptionResponseDto;
import com.example.PetAdoption.entity.AdoptionRequest;
import com.example.PetAdoption.service.AdoptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/adoptions")
@Tag(name = "Adoptions", description = "Submit and manage pet adoption requests")
public class AdoptionController {

    private final AdoptionService adoptionService;

    @Autowired
    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    @PostMapping
    @Operation(summary = "Submit an adoption request for a pet (logged-in user)")
    public ResponseEntity<AdoptionResponseDto> createRequest(Authentication authentication,
                                                               @Valid @RequestBody AdoptionRequestCreateDto dto) {
        AdoptionRequest request = adoptionService.createRequest(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AdoptionResponseDto.fromEntity(request));
    }

    @GetMapping("/my")
    @Operation(summary = "View my own adoption requests")
    public List<AdoptionResponseDto> getMyRequests(Authentication authentication) {
        return adoptionService.getMyRequests(authentication.getName()).stream()
                .map(AdoptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping
    @Operation(summary = "View all adoption requests (ADMIN only)")
    public List<AdoptionResponseDto> getAllRequests() {
        return adoptionService.getAllRequests().stream()
                .map(AdoptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve an adoption request (ADMIN only)")
    public ResponseEntity<AdoptionResponseDto> approve(@PathVariable long id) {
        AdoptionRequest request = adoptionService.approveRequest(id);
        return ResponseEntity.ok(AdoptionResponseDto.fromEntity(request));
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject an adoption request (ADMIN only)")
    public ResponseEntity<AdoptionResponseDto> reject(@PathVariable long id) {
        AdoptionRequest request = adoptionService.rejectRequest(id);
        return ResponseEntity.ok(AdoptionResponseDto.fromEntity(request));
    }
}
