package com.example.PetAdoption.controller;

import com.example.PetAdoption.dto.PetImageResponse;
import com.example.PetAdoption.dto.PetRequest;
import com.example.PetAdoption.dto.PetResponse;
import com.example.PetAdoption.entity.Pet;
import com.example.PetAdoption.entity.PetImages;
import com.example.PetAdoption.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pets")
@Tag(name = "Pets", description = "Browse and manage adoptable pets")
public class PetController {

    private final PetService petService;

    @Autowired
    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping
    @Operation(summary = "List pets. Optionally filter by status, species, or search query.")
    public List<PetResponse> getAllPets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String q) {

        List<Pet> pets;
        if (q != null && !q.isBlank()) {
            pets = petService.searchPets(q);
        } else if (status != null && !status.isBlank()) {
            pets = petService.getPetsByStatus(status);
        } else if (species != null && !species.isBlank()) {
            pets = petService.getPetsBySpecies(species);
        } else {
            pets = petService.getAllPets();
        }
        return pets.stream().map(PetResponse::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "List pets created by the currently signed-in user")
    public List<PetResponse> getMyPets(Principal principal) {
        if (principal == null) {
            return List.of();
        }
        return petService.getPetsByCreator(principal.getName()).stream()
                .map(PetResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Add a new pet (any signed-in user may list a pet)")
    public ResponseEntity<PetResponse> addPet(@Valid @RequestBody PetRequest request, Principal principal) {
        Pet pet = petService.addPet(request, principal == null ? null : principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(PetResponse.fromEntity(pet));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Update a pet's details (owner or ADMIN)")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequest request,
                                                Principal principal, org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        Pet pet = petService.updatePet(id, request, principal == null ? null : principal.getName(), isAdmin);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update just a pet's status (ADMIN only)")
    public ResponseEntity<PetResponse> updatePetStatus(@PathVariable Long id, @RequestParam String status) {
        Pet pet = petService.updatePetStatus(id, status);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete a pet (owner or ADMIN)")
    public ResponseEntity<String> deletePet(@PathVariable Long id, Principal principal,
                                            org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        petService.deletePet(id, principal == null ? null : principal.getName(), isAdmin);
        return ResponseEntity.ok("Pet deleted successfully");
    }

    // ---------------------------------------------------------------
    // Cloudinary-backed image endpoints
    // ---------------------------------------------------------------

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Upload/replace a pet's main photo (owner or ADMIN). Multipart 'file' field.")
    public ResponseEntity<PetResponse> uploadMainImage(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file,
                                                         Principal principal,
                                                         org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        Pet pet = petService.setMainImage(id, file, principal == null ? null : principal.getName(), isAdmin);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Add a gallery photo for a pet (owner or ADMIN). Multipart 'file' field.")
    public ResponseEntity<PetImageResponse> addGalleryImage(@PathVariable Long id,
                                                              @RequestParam("file") MultipartFile file,
                                                              Principal principal,
                                                              org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        PetImages image = petService.addGalleryImage(id, file, principal == null ? null : principal.getName(), isAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(PetImageResponse.fromEntity(image));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "List a pet's gallery photos")
    public List<PetImageResponse> getGalleryImages(@PathVariable Long id) {
        return petService.getGalleryImages(id).stream()
                .map(PetImageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Operation(summary = "Delete one gallery photo (owner or ADMIN)")
    public ResponseEntity<String> deleteGalleryImage(@PathVariable Long id, @PathVariable long imageId,
                                                      Principal principal,
                                                      org.springframework.security.core.Authentication auth) {
        boolean isAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        petService.deleteGalleryImage(id, imageId, principal == null ? null : principal.getName(), isAdmin);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
