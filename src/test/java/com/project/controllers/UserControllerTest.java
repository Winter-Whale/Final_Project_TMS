package com.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.config.SpringSecurity;
import com.project.exceptions.UserNotFoundException;
import com.project.filters.JwtFilter;
import com.project.models.User;
import com.project.models.dto.user.UserCreateDTO;
import com.project.models.dto.user.UserUpdateDTO;
import com.project.services.UserService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = UserController.class,
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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private ObjectMapper objectMapper;

    private User user;
    private List<User> userList;
    private UserCreateDTO createDTO;
    private UserUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        user = new User();
        user.setId(1);
        user.setFullName("John Doe");
        user.setPhone("+79123456789");
        user.setEmail("john@example.com");
        user.setAge(25);

        User user2 = new User();
        user2.setId(2);
        user2.setFullName("Jane Smith");
        user2.setPhone("+79876543210");
        user2.setEmail("jane@example.com");
        user2.setAge(30);

        userList = List.of(user, user2);

        createDTO = new UserCreateDTO();
        createDTO.setFullName("New User");
        createDTO.setPhone("+79998887766");
        createDTO.setEmail("new@example.com");
        createDTO.setAge(22);

        updateDTO = new UserUpdateDTO();
        updateDTO.setId(1);
        updateDTO.setFullName("Updated Name");
        updateDTO.setPhone("+71112223344");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setAge(28);
        updateDTO.setUsername("new_username");
        updateDTO.setPassword("newPassword");
    }

    @Test
    void getAllUsers_ShouldReturnList() throws Exception {
        when(userService.getAllUsers()).thenReturn(userList);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].fullName").value("Jane Smith"));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() throws Exception {
        when(userService.getUserById(1)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("+79123456789"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(userService.getUserById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getInfoAboutMyself_ShouldReturnUser_WhenExists() throws Exception {
        when(userService.getInfoAboutMyself()).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/info/myself"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void getInfoAboutMyself_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getInfoAboutMyself()).thenReturn(Optional.empty());

        mockMvc.perform(get("/users/info/myself"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_ShouldReturnCreated_WhenValid() throws Exception {
        when(userService.createUser(any(UserCreateDTO.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UserCreateDTO invalid = new UserCreateDTO();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_ShouldReturnOk_WhenValid() throws Exception {
        when(userService.updateUser(any(UserUpdateDTO.class))).thenReturn(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        UserUpdateDTO invalid = new UserUpdateDTO();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserById_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        doNothing().when(userService).deleteUserById(1);

        mockMvc.perform(delete("/users/{id}", 1))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUserById(99);

        mockMvc.perform(delete("/users/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
