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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Submit an adoption request for a pet (logged-in user)")
    public ResponseEntity<AdoptionResponseDto> createRequest(Principal principal,
                                                               @Valid @RequestBody AdoptionRequestCreateDto dto) {
        AdoptionRequest request = adoptionService.createRequest(principal.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AdoptionResponseDto.fromEntity(request));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "View my own adoption requests")
    public List<AdoptionResponseDto> getMyRequests(Principal principal) {
        return adoptionService.getMyRequests(principal.getName()).stream()
                .map(AdoptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "View adoption requests for pets I listed (owner or ADMIN)")
    public List<AdoptionResponseDto> getReceivedRequests(Principal principal,
                                                         org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return adoptionService.getReceivedRequests(principal.getName(), isAdmin).stream()
                .map(AdoptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "View all adoption requests (ADMIN only)")
    public List<AdoptionResponseDto> getAllRequests() {
        return adoptionService.getAllRequests().stream()
                .map(AdoptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Approve an adoption request (owner of the pet or ADMIN)")
    public ResponseEntity<AdoptionResponseDto> approve(@PathVariable long id, Principal principal,
                                                       org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        AdoptionRequest request = adoptionService.approveRequest(id, principal.getName(), isAdmin);
        return ResponseEntity.ok(AdoptionResponseDto.fromEntity(request));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Reject an adoption request (owner of the pet or ADMIN)")
    public ResponseEntity<AdoptionResponseDto> reject(@PathVariable long id, Principal principal,
                                                      org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        AdoptionRequest request = adoptionService.rejectRequest(id, principal.getName(), isAdmin);
        return ResponseEntity.ok(AdoptionResponseDto.fromEntity(request));
    }
}
