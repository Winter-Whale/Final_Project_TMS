package com.project.controllers;

import com.project.models.dto.Booking.BookingRequestDTO;
import com.project.models.dto.Booking.BookingResponseDTO;
import com.project.services.BookingService;
import com.project.services.CurrentUserService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
@Tag(name = "Бронирования", description = "Управление бронированиями парковочных мест")
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "Создать бронирование",
            description = "Арендатор создаёт бронирование на конкретное парковочное место на выбранный интервал времени.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Бронирование успешно создано",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BookingResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные (место занято, время невалидно и т.д.)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Парковочное место или пользователь не найдены",
                    content = @Content)
    })
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Parameter(description = "Данные для бронирования", required = true)
            @RequestBody @Valid BookingRequestDTO req) {
        int renterId = currentUserService.getCurrentUser().getId();
        BookingResponseDTO responseDTO = bookingService.createBooking(req, renterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}
