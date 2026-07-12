package com.project.controllers;

import com.project.models.Role;
import com.project.models.Security;
import com.project.models.User;
import com.project.models.dto.User.AuthRequestDTO;
import com.project.models.dto.User.AuthResponseDTO;
import com.project.models.dto.User.RegistrationDTO;
import com.project.models.dto.User.SecurityUpdateDTO;
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
@Tag(name = "Аутентификация и безопасность", description = "Регистрация, вход, управление учётными данными")
public class SecurityController {

    private final SecurityService securityService;

    @PostMapping("/registration/owner")
    @Operation(summary = "Регистрация владельца",
            description = "Создаёт нового пользователя с ролью OWNER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или логин/телефон уже заняты")
    })
    public ResponseEntity<User> registrationOwner(
            @Parameter(description = "Данные для регистрации", required = true)
            @RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.OWNER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/registration/renter")
    @Operation(summary = "Регистрация арендатора",
            description = "Создаёт нового пользователя с ролью RENTER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные или логин/телефон уже заняты")
    })
    public ResponseEntity<User> registrationRenter(
            @Parameter(description = "Данные для регистрации", required = true)
            @RequestBody @Valid RegistrationDTO registrationDTO) {
        User createdUser = securityService.registration(registrationDTO, Role.RENTER);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить данные безопасности пользователя",
            description = "Возвращает запись Security по ID (доступно только ADMIN).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Найдено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Security.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено")
    })
    public ResponseEntity<Security> getSecurityById(
            @Parameter(description = "ID записи Security", required = true, example = "1")
            @PathVariable Integer id) {
        Optional<Security> security = securityService.getSecurityById(id);
        return security
                .map(value -> ResponseEntity.ok(value))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/update")
    @Operation(summary = "Обновить учётные данные",
            description = "Позволяет изменить логин и пароль, предварительно проверив текущий пароль.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Обновлено успешно"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль или логин уже занят")
    })
    public ResponseEntity<Void> updateSecurity(
            @Parameter(description = "ID записи Security", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Данные для обновления (текущий пароль, новый логин, новый пароль)", required = true)
            @RequestBody @Valid SecurityUpdateDTO dto) {
        securityService.updateSecurity(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/generate")
    @Operation(summary = "Аутентификация и получение JWT",
            description = "Отправьте логин и пароль, чтобы получить JWT-токен для доступа к защищённым эндпоинтам.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Токен успешно сгенерирован",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные учётные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    public ResponseEntity<AuthResponseDTO> generateJWT(
            @Parameter(description = "Учётные данные пользователя", required = true)
            @Valid @RequestBody AuthRequestDTO authRequestDTO) {
        Optional<AuthResponseDTO> jwt = securityService.generateJWT(authRequestDTO);
        if (jwt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(jwt.get(), HttpStatus.CREATED);
    }
}
