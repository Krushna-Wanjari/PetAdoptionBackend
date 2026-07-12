package com.example.PetAdoption.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String tokenType = "Bearer";
    private long userId;
    private String name;
    private String email;
    private String role;

    public JwtResponse(String token, long userId, String name, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
