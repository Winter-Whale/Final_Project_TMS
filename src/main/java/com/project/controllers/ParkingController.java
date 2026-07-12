package com.project.controllers;

import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.services.ParkingService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/parking")
@Tag(name = "Парковочные места", description = "Управление парковочными местами")
public class ParkingController {

    private final ParkingService parkingService;

    @GetMapping
    @Operation(summary = "Получить все парковочные места",
            description = "Возвращает список всех мест с их статусами (доступно/занято).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class)))
    })
    public ResponseEntity<List<ParkingResponseDTO>> getAllSpots() {
        return ResponseEntity.ok(parkingService.getAllSpots());
    }

    @GetMapping("/spot/{id}")
    @Operation(summary = "Получить место по ID",
            description = "Возвращает информацию о конкретном парковочном месте.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Место найдено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Место не найдено",
                    content = @Content)
    })
    public ResponseEntity<ParkingResponseDTO> getSpotById(
            @Parameter(description = "ID парковочного места", required = true, example = "1")
            @PathVariable Integer id) {
        ParkingResponseDTO responseDTO = parkingService.getSpotById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "Получить все места пользователя",
            description = "Возвращает список мест, принадлежащих пользователю с указанным ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден",
                    content = @Content)
    })
    public ResponseEntity<List<ParkingResponseDTO>> getSpotByUser(
            @Parameter(description = "ID пользователя", required = true, example = "2")
            @PathVariable Integer id) {
        return ResponseEntity.ok(parkingService.getSpotByUser(id));
    }

    @PostMapping
    @Operation(summary = "Создать парковочное место",
            description = "Владелец или администратор создаёт новое место. Доступно только для ролей OWNER и ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Место создано",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён (не OWNER/ADMIN)")
    })
    public ResponseEntity<ParkingResponseDTO> createSpot(
            @Parameter(description = "Данные для создания места", required = true)
            @RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO createSpot = parkingService.createSpot(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createSpot);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить парковочное место",
            description = "Обновляет информацию о месте. Доступно владельцу места или администратору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Место обновлено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParkingResponseDTO.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Место не найдено")
    })
    public ResponseEntity<ParkingResponseDTO> updateSpot(
            @Parameter(description = "ID места", required = true, example = "1")
            @PathVariable Integer id,
            @Parameter(description = "Новые данные места", required = true)
            @RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO update = parkingService.updateSpot(id, requestDTO);
        return ResponseEntity.ok(update);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить парковочное место",
            description = "Удаляет место по ID. Доступно владельцу или администратору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Место удалено"),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав"),
            @ApiResponse(responseCode = "404", description = "Место не найдено")
    })
    public ResponseEntity<Void> deleteSpot(
            @Parameter(description = "ID места", required = true, example = "1")
            @PathVariable Integer id) {
        parkingService.deleteSpot(id);
        return ResponseEntity.noContent().build();
    }
}
