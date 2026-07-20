package com.project.controllers;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.user.AuthRequestDTO;
import com.project.models.dto.user.AuthResponseDTO;
import com.project.models.dto.user.RegistrationDTO;
import com.project.models.dto.user.SecurityUpdateDTO;
import com.project.services.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/security")
@Tag(name = "Authentication and Security", description = "Registration, login, account management")
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping("/registration/owner")
    @Operation(summary = "Owner registration",
            description = "Creates a new user with the OWNER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The user is registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect data or login/phone number is already taken")
    })
    public ResponseEntity<User> registrationOwner(
            @Parameter(description = "Registration details", required = true)
            @RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/registration/renter")
    @Operation(summary = "Renter registration",
            description = "Creates a new user with the RENTER role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The user is registered",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect data or login/phone number is already taken")
    })
    public ResponseEntity<User> registrationRenter(
            @Parameter(description = "Registration details", required = true)
            @RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.RENTER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user security data",
            description = "Returns a Security record by ID (available only to ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Security.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Security> getSecurityById(
            @Parameter(description = "Security Record ID", required = true, example = "1")
            @PathVariable Integer id) {
        Optional<Security> security = securityService.getSecurityById(id);
        return security
                .map(value -> ResponseEntity.ok(value))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/update")
    @Operation(summary = "Update credentials",
            description = "Allows you to change your login and password, after checking the current password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated successfully"),
            @ApiResponse(responseCode = "400", description = "The current password is incorrect or the login is already taken.")
    })
    public ResponseEntity<Void> updateSecurity(
            @Parameter(description = "Security Record ID", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Update details (current password, new login, new password)", required = true)
            @RequestBody @Valid SecurityUpdateDTO dto) {
        securityService.updateSecurity(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    @Operation(summary = "Authentication and obtaining JWT",
            description = "Submit your login and password to receive a JWT token for accessing secure endpoints.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The token was successfully generated.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AuthResponseDTO> generateJWT(
            @Parameter(description = "User credentials", required = true)
            @Valid @RequestBody AuthRequestDTO authRequestDTO) {
        Optional<AuthResponseDTO> jwt = securityService.generateJWT(authRequestDTO);
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(jwt.get(), HttpStatus.CREATED);
    }
}
