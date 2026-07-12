package com.example.PetAdoption.dto;

import com.example.PetAdoption.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {
    private long userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
