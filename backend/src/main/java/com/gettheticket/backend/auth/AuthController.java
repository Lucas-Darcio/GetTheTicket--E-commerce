package com.gettheticket.backend.auth;

import com.gettheticket.backend.auth.dto.LoginRequestDTO;
import com.gettheticket.backend.auth.dto.LoginResponseDTO;
import com.gettheticket.backend.security.JwtService;
import com.gettheticket.backend.user.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
            );
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails.getUsername(), userDetails.getRole());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}