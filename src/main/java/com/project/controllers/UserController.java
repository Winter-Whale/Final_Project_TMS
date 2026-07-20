package com.project.controllers;

import com.project.models.User;
import com.project.models.dto.user.UserCreateDTO;
import com.project.models.dto.user.UserUpdateDTO;
import com.project.services.UserService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Get all users",
            description = "Returns a list of all users (available only to ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The list has been received.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)))
    })
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID",
            description = "Returns user data (available to ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/info/myself")
    @Operation(summary = "Get information about yourself",
            description = "Returns the data of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Data received",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> getInfoAboutMyself() {
        Optional<User> user = userService.getInfoAboutMyself();
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Create a user",
            description = "Creates a new user (for ADMIN only).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect data")
    })
    public ResponseEntity<User> createUser(
            @Parameter(description = "User data", required = true)
            @RequestBody @Valid UserCreateDTO userRequest) {
        User createUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }

    @PutMapping
    @Operation(summary = "Update user",
            description = "Updates an existing user's data. Available to ADMIN or the user themselves.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Incorrect data or the phone number is already busy"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "New user data", required = true)
            @RequestBody @Valid UserUpdateDTO userRequest) {
        User user = userService.updateUser(userRequest);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user",
            description = "Deletes a user by ID. Only available to ADMIN users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Integer id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
