package com.project.controllers;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.User.AuthRequestDTO;
import com.project.models.dto.User.AuthResponseDTO;
import com.project.models.dto.User.RegistrationDTO;
import com.project.models.dto.User.SecurityUpdateDTO;
import com.project.services.SecurityService;
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
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping("/registration/owner")
    public ResponseEntity<User> registrationOwner(@RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/registration/renter")
    public ResponseEntity<User> registrationRenter(@RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.RENTER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Security> getSecurityById(@PathVariable Integer id) {
        Optional<Security> security = securityService.getSecurityById(id);
        return security
                .map(value -> ResponseEntity.ok(value))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<Void> updateSecurity(@PathVariable Integer id, @RequestBody @Valid SecurityUpdateDTO dto) {
        securityService.updateSecurity(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    public ResponseEntity<AuthResponseDTO> generateJWT(@Valid @RequestBody AuthRequestDTO authRequestDTO) {
        Optional<AuthResponseDTO> jwt = securityService.generateJWT(authRequestDTO);
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(jwt.get(), HttpStatus.CREATED);
    }
}
