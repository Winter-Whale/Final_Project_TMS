package com.project.controllers;

import com.project.models.User;
import com.project.models.dto.User.UserCreateDTO;
import com.project.models.dto.User.UserUpdateDTO;
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
    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей (доступно только ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class)))
    })
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя (доступно ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь найден",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    public ResponseEntity<User> getUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Integer id) {
        Optional<User> user = userService.getUserById(id);
        return user
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/info/myself")
    @Operation(summary = "Получить информацию о себе",
            description = "Возвращает данные текущего аутентифицированного пользователя.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Данные получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<User> getInfoAboutMyself() {
        Optional<User> user = userService.getInfoAboutMyself();
        if (user.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    @PostMapping
    @Operation(summary = "Создать пользователя",
            description = "Создаёт нового пользователя (только для ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")
    })
    public ResponseEntity<User> createUser(
            @Parameter(description = "Данные пользователя", required = true)
            @RequestBody @Valid UserCreateDTO userRequest) {
        User createUser = userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }

    @PutMapping
    @Operation(summary = "Обновить пользователя",
            description = "Обновляет данные существующего пользователя. Доступно для ADMIN или самому пользователю.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Обновлён",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или телефон уже занят"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<User> updateUser(
            @Parameter(description = "Новые данные пользователя", required = true)
            @RequestBody @Valid UserUpdateDTO userRequest) {
        User user = userService.updateUser(userRequest);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя",
            description = "Удаляет пользователя по ID. Доступно только ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Удалён"),
            @ApiResponse(responseCode = "404", description = "Не найден")
    })
    public ResponseEntity<Void> deleteUserById(
            @Parameter(description = "ID пользователя", required = true, example = "1")
            @PathVariable Integer id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
