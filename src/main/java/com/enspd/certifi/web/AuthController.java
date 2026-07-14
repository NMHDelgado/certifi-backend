package com.enspd.certifi.web;

import com.enspd.certifi.domain.entity.AppUser;
import com.enspd.certifi.dto.request.LoginRequest;
import com.enspd.certifi.dto.response.LoginResponse;
import com.enspd.certifi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUser user = (AppUser) authentication.getPrincipal();
        String token = jwtService.generateToken(user);

        return new LoginResponse(user.getId(), user.getEmail(), user.getRole(), token);
    }
}
