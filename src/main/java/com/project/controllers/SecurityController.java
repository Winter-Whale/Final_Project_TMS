package com.project.controllers;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.UserUpdateException;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.User.AuthRequestDTO;
import com.project.models.dto.User.AuthResponseDTO;
import com.project.models.dto.User.RegistrationDTO;
import com.project.models.dto.User.SecurityUpdateDTO;
import com.project.services.SecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/security")
public class SecurityController {
    private final SecurityService securityService;

    @Autowired
    public SecurityController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping("/registration/owner")
    public ResponseEntity<User> registrationOwner(@RequestBody @Valid RegistrationDTO registrationDTO) throws RegistrationException {
        User createdUser = securityService.registration(registrationDTO, Role.OWNER);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/registration/renter")
    public ResponseEntity<User> registrationRenter(@RequestBody @Valid RegistrationDTO registrationDTO) throws RegistrationException {
        User createdUser = securityService.registration(registrationDTO, Role.RENTER);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Security> getSecurityById(@PathVariable Integer id) {
        Optional<Security> security = securityService.getSecurityById(id);
        if (security.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(security.get(), HttpStatus.OK);
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<String> updateSecurity(@PathVariable Integer id, @RequestBody @Valid SecurityUpdateDTO dto){
        try{
            securityService.updateSecurity(id, dto);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UserNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (UserUpdateException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
