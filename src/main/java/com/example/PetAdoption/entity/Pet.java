package com.example.PetAdoption.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    @Column(name = "name")
    private String name;

    @NotBlank(message = "Species is required")
    @Size(max = 50)
    @Column(name = "species")
    private String species;

    @Size(max = 100)
    @Column(name = "breed")
    private String breed;

    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 50, message = "Age must be realistic")
    @Column(name = "age")
    private int age;

    public enum Gender { MALE, FEMALE }

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    public enum PetStatus { AVAILABLE, ADOPTED, PENDING }

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PetStatus status = PetStatus.AVAILABLE;

    @Size(max = 2000, message = "Description must be under 2000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ---------- Listing details captured by the "List a Pet" form ----------

    @Size(max = 100, message = "City must be under 100 characters")
    @Column(name = "city")
    private String city;

    @Size(max = 50, message = "Weight must be under 50 characters")
    @Column(name = "weight")
    private String weight;

    @Size(max = 50, message = "Color must be under 50 characters")
    @Column(name = "color")
    private String color;

    // Boxed rather than primitive: these columns were added after the table
    // already held rows, so MySQL backfills them as NULL and a primitive
    // boolean would blow up with "Null value was assigned to a property of
    // primitive type" the moment an existing pet is read.
    @Column(name = "vaccinated", columnDefinition = "boolean default false")
    private Boolean vaccinated = Boolean.FALSE;

    @Column(name = "sterilized", columnDefinition = "boolean default false")
    private Boolean sterilized = Boolean.FALSE;

    @Size(max = 2000, message = "Reason must be under 2000 characters")
    @Column(name = "reason_for_rehoming", length = 2000)
    private String reasonForRehoming;

    // ---------- Owner contact, shown on the pet detail page ----------

    @Size(max = 100, message = "Owner name must be under 100 characters")
    @Column(name = "owner_name")
    private String ownerName;

    @Size(max = 30, message = "Owner phone must be under 30 characters")
    @Column(name = "owner_phone")
    private String ownerPhone;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PetImages> images = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "pet", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<AdoptionRequest> adoptionRequests = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnore
    @ToString.Exclude
    private User createdBy;

    public Pet(String name, String species, String breed, int age, Gender gender, PetStatus status, String description, String imageUrl) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.description = description;
        this.imageUrl = imageUrl;
    }
}
