package com.example.PetAdoption.controller;

import com.example.PetAdoption.dto.JwtResponse;
import com.example.PetAdoption.dto.LoginRequest;
import com.example.PetAdoption.dto.RegisterRequest;
import com.example.PetAdoption.dto.UserResponse;
import com.example.PetAdoption.entity.User;
import com.example.PetAdoption.security.JwtUtil;
import com.example.PetAdoption.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and login")
@SecurityRequirements // these endpoints don't need a bearer token
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account (role USER)")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT bearer token")
    public ResponseEntity<JwtResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userService.getUserByEmail(request.getEmail());
        JwtResponse response = new JwtResponse(token, user.getUserId(), user.getName(), user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(response);
    }
}
