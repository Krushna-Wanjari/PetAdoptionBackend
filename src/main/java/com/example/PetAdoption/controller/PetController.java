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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "List pets. Optionally filter by status or species.")
    public List<PetResponse> getAllPets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String species) {

        List<Pet> pets;
        if (status != null && !status.isBlank()) {
            pets = petService.getPetsByStatus(status);
        } else if (species != null && !species.isBlank()) {
            pets = petService.getPetsBySpecies(species);
        } else {
            pets = petService.getAllPets();
        }
        return pets.stream().map(PetResponse::fromEntity).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single pet's details")
    public ResponseEntity<PetResponse> getPetById(@PathVariable Long id) {
        Pet pet = petService.getPetById(id);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @PostMapping
    @Operation(summary = "Add a new pet (ADMIN only)")
    public ResponseEntity<PetResponse> addPet(@Valid @RequestBody PetRequest request) {
        Pet pet = petService.addPet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PetResponse.fromEntity(pet));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a pet's details (ADMIN only)")
    public ResponseEntity<PetResponse> updatePet(@PathVariable Long id, @Valid @RequestBody PetRequest request) {
        Pet pet = petService.updatePet(id, request);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update just a pet's status (ADMIN only)")
    public ResponseEntity<PetResponse> updatePetStatus(@PathVariable Long id, @RequestParam String status) {
        Pet pet = petService.updatePetStatus(id, status);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a pet (ADMIN only)")
    public ResponseEntity<String> deletePet(@PathVariable Long id) {
        petService.deletePet(id);
        return ResponseEntity.ok("Pet deleted successfully");
    }

    // ---------------------------------------------------------------
    // Cloudinary-backed image endpoints
    // ---------------------------------------------------------------

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload/replace a pet's main photo (ADMIN only). "
            + "Send as multipart/form-data with the file under the 'file' field.")
    public ResponseEntity<PetResponse> uploadMainImage(@PathVariable Long id,
                                                         @RequestParam("file") MultipartFile file) {
        Pet pet = petService.setMainImage(id, file);
        return ResponseEntity.ok(PetResponse.fromEntity(pet));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add a gallery photo for a pet (ADMIN only). Multipart 'file' field.")
    public ResponseEntity<PetImageResponse> addGalleryImage(@PathVariable Long id,
                                                              @RequestParam("file") MultipartFile file) {
        PetImages image = petService.addGalleryImage(id, file);
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
    @Operation(summary = "Delete one gallery photo (ADMIN only)")
    public ResponseEntity<String> deleteGalleryImage(@PathVariable Long id, @PathVariable long imageId) {
        petService.deleteGalleryImage(id, imageId);
        return ResponseEntity.ok("Image deleted successfully");
    }
}
