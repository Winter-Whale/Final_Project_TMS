package com.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.config.SpringSecurity;
import com.project.exceptions.SpotNotFoundException;
import com.project.filters.JwtFilter;
import com.project.models.Status;
import com.project.models.User;
import com.project.models.dto.Booking.BookingRequestDTO;
import com.project.models.dto.Booking.BookingResponseDTO;
import com.project.services.BookingService;
import com.project.services.CurrentUserService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = BookingController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class,
                OAuth2ResourceServerAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class,
                Saml2RelyingPartyAutoConfiguration.class
        },
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = SpringSecurity.class
        ),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = JwtFilter.class
                )
        }
)
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private CurrentUserService currentUserService;
    private ObjectMapper objectMapper;
    private User testUser;
    private BookingRequestDTO validRequest;
    private BookingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUser = new User();
        testUser.setId(1);

        validRequest = new BookingRequestDTO();
        validRequest.setSpotId(100);
        validRequest.setStartTime(LocalDateTime.now().plusHours(1));
        validRequest.setEndTime(LocalDateTime.now().plusHours(3));

        responseDTO = new BookingResponseDTO();
        responseDTO.setBookingId(42);
        responseDTO.setSpotId(100);
        responseDTO.setAddress("ул. Пушкина, д. 10");
        responseDTO.setStartTime(validRequest.getStartTime());
        responseDTO.setEndTime(validRequest.getEndTime());
        responseDTO.setTotalPrice(new BigDecimal("150.00"));
        responseDTO.setSpotStatus(Status.BUSY);
    }

    @Test
    void createBooking_ShouldReturnCreated_WhenRequestIsValid() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(bookingService.createBooking(any(BookingRequestDTO.class), eq(1)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(42))
                .andExpect(jsonPath("$.spotId").value(100))
                .andExpect(jsonPath("$.address").value("ул. Пушкина, д. 10"))
                .andExpect(jsonPath("$.totalPrice").value(150.00))
                .andExpect(jsonPath("$.spotStatus").value("BUSY"));
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenStartTimeIsInPast() throws Exception {
        BookingRequestDTO invalidRequest = new BookingRequestDTO();
        invalidRequest.setSpotId(100);
        invalidRequest.setStartTime(LocalDateTime.now().minusHours(1));
        invalidRequest.setEndTime(LocalDateTime.now().plusHours(3));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ShouldReturnBadRequest_WhenEndTimeIsInPast() throws Exception {
        BookingRequestDTO invalidRequest = new BookingRequestDTO();
        invalidRequest.setSpotId(100);
        invalidRequest.setStartTime(LocalDateTime.now().plusHours(1));
        invalidRequest.setEndTime(LocalDateTime.now().minusHours(1));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ShouldReturnNotFound_WhenParkingSpotNotFound() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(bookingService.createBooking(any(BookingRequestDTO.class), eq(1)))
                .thenThrow(new SpotNotFoundException("Parking spot not found"));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_ShouldCallServiceWithCorrectUserId() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(testUser);
        when(bookingService.createBooking(any(BookingRequestDTO.class), eq(1)))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());

        verify(bookingService).createBooking(any(BookingRequestDTO.class), eq(1));
    }
}
