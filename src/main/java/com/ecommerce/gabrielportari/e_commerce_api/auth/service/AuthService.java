package com.ecommerce.gabrielportari.e_commerce_api.auth.service;

import com.ecommerce.gabrielportari.e_commerce_api.auth.dto.LoginRequest;
import com.ecommerce.gabrielportari.e_commerce_api.auth.dto.LoginResponse;
import com.ecommerce.gabrielportari.e_commerce_api.auth.security.JwtUtil;
import com.ecommerce.gabrielportari.e_commerce_api.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("E-mail ou senha inválidos");
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .orElseThrow(() -> new InvalidCredentialsException("Usuário sem permissão definida"));

        String token = jwtUtil.generateToken(authentication.getName(), role);
        return new LoginResponse(token);
    }
}
