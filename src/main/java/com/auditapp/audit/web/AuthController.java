package com.auditapp.audit.web;

import com.auditapp.audit.service.AuthService;
import com.auditapp.audit.web.dto.LoginRequest;
import com.auditapp.audit.web.dto.SessionResponse;
import com.auditapp.audit.web.dto.SignupRequest;
import com.auditapp.audit.web.dto.SwitchOrganizationRequest;
import com.auditapp.audit.web.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create an account and its first organization, and sign in")
    @SecurityRequirements // public: no token needed
    public TokenResponse signup(@Valid @RequestBody SignupRequest request) {
        return auth.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Sign in. Returns a token for the given organization, or the first one by name")
    @SecurityRequirements // public: no token needed
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "The signed-in user, their current organization and all of their organizations")
    public SessionResponse me() {
        return auth.session();
    }

    @PostMapping("/switch")
    @Operation(summary = "Get a token for another organization the user belongs to")
    public TokenResponse switchOrganization(@Valid @RequestBody SwitchOrganizationRequest request) {
        return auth.switchOrganization(request.organizationId());
    }
}
