package com.project.models.dto.Booking;

import com.project.models.Status;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BookingResponseDTO {
    private Integer bookingId;
    private Integer spotId;
    private String address;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal totalPrice;
    private Status spotStatus;
}
