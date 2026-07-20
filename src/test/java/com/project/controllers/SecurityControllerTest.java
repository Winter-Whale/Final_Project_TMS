package com.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.config.SpringSecurity;
import com.project.filters.JwtFilter;
import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.user.AuthRequestDTO;
import com.project.models.dto.user.AuthResponseDTO;
import com.project.models.dto.user.RegistrationDTO;
import com.project.models.dto.user.SecurityUpdateDTO;
import com.project.services.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.saml2.Saml2RelyingPartyAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = SecurityController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                Saml2RelyingPartyAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = SpringSecurity.class
                ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
public class SecurityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SecurityService securityService;

    private ObjectMapper objectMapper;

    private RegistrationDTO registrationDTO;
    private User createdUser;
    private Security security;
    private SecurityUpdateDTO updateDTO;
    private AuthRequestDTO authRequest;
    private AuthResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        registrationDTO = new RegistrationDTO();
        registrationDTO.setFullName("John Doe");
        registrationDTO.setPhone("+79123456789");
        registrationDTO.setEmail("john@example.com");
        registrationDTO.setAge(25);
        registrationDTO.setUsername("john_doe");
        registrationDTO.setPassword("password123");

        createdUser = new User();
        createdUser.setId(1);
        createdUser.setFullName("John Doe");
        createdUser.setPhone("+79123456789");
        createdUser.setEmail("john@example.com");
        createdUser.setAge(25);

        security = new Security();
        security.setId(1);
        security.setUsername("john_doe");
        security.setPassword("encodedPassword");
        security.setRole(Role.OWNER);

        updateDTO = new SecurityUpdateDTO();
        updateDTO.setCurrentPassword("oldPass");
        updateDTO.setNewUserName("new_john");
        updateDTO.setNewPassword("newPass123");

        authRequest = new AuthRequestDTO();
        authRequest.setUsername("john_doe");
        authRequest.setPassword("password123");

        authResponse = new AuthResponseDTO("jwt-token-xyz");
    }

    @Test
    void registrationOwner_ShouldReturnCreated() throws Exception {
        when(securityService.registration(any(RegistrationDTO.class), eq(Role.OWNER)))
                .thenReturn(createdUser);

        mockMvc.perform(post("/security/registration/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("+79123456789"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void registrationOwner_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        RegistrationDTO invalid = new RegistrationDTO();

        mockMvc.perform(post("/security/registration/owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationRenter_ShouldReturnCreated() throws Exception {
        User renterUser = new User();
        renterUser.setId(2);
        renterUser.setFullName("Jane Doe");
        renterUser.setPhone("+79876543210");
        renterUser.setEmail("jane@example.com");
        renterUser.setAge(30);

        when(securityService.registration(any(RegistrationDTO.class), eq(Role.RENTER)))
                .thenReturn(renterUser);

        mockMvc.perform(post("/security/registration/renter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.phone").value("+79876543210"));
    }

    @Test
    void registrationRenter_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        RegistrationDTO invalid = new RegistrationDTO();

        mockMvc.perform(post("/security/registration/renter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSecurityById_ShouldReturnSecurity_WhenExists() throws Exception {
        when(securityService.getSecurityById(1)).thenReturn(Optional.of(security));

        mockMvc.perform(get("/security/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("john_doe"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void getSecurityById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(securityService.getSecurityById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/security/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSecurity_ShouldReturnOk_WhenValid() throws Exception {
        doNothing().when(securityService).updateSecurity(eq(1), any(SecurityUpdateDTO.class));

        mockMvc.perform(put("/security/{id}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void updateSecurity_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        SecurityUpdateDTO invalid = new SecurityUpdateDTO();

        mockMvc.perform(put("/security/{id}/update", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateJWT_ShouldReturnToken_WhenCredentialsValid() throws Exception {
        when(securityService.generateJWT(any(AuthRequestDTO.class)))
                .thenReturn(Optional.of(authResponse));

        mockMvc.perform(post("/security/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-xyz"));
    }

    @Test
    void generateJWT_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(securityService.generateJWT(any(AuthRequestDTO.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/security/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateJWT_ShouldReturnBadRequest_WhenInvalidCredentials() throws Exception {
        AuthRequestDTO invalid = new AuthRequestDTO();

        mockMvc.perform(post("/security/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
}
