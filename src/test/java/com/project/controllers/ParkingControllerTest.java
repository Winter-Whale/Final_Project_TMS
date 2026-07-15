package com.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.config.SpringSecurity;
import com.project.exceptions.SpotNotFoundException;
import com.project.exceptions.UserNotFoundException;
import com.project.filters.JwtFilter;
import com.project.models.Status;
import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.services.ParkingService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = ParkingController.class,
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
public class ParkingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ParkingService parkingService;

    private ObjectMapper objectMapper;
    private ParkingRequestDTO validRequest;
    private ParkingResponseDTO responseDTO;
    private List<ParkingResponseDTO> responseList;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validRequest = new ParkingRequestDTO();
        validRequest.setAddress("ул. Ленина, д. 5");
        validRequest.setDescription("Крытая парковка с охраной");
        validRequest.setPricePerHour(BigDecimal.valueOf(150.00));
        validRequest.setStatus(Status.FREE);
        validRequest.setOwnerId(10);

        responseDTO = new ParkingResponseDTO();
        responseDTO.setId(1);
        responseDTO.setAddress("ул. Ленина, д. 5");
        responseDTO.setDescription("Крытая парковка с охраной");
        responseDTO.setPricePerHour(BigDecimal.valueOf(150.00));
        responseDTO.setStatus(Status.FREE);
        responseDTO.setOwnerId(10);
        responseDTO.setOwnerFullName("Иван Петров");

        ParkingResponseDTO responseDTO2 = new ParkingResponseDTO();
        responseDTO2.setId(2);
        responseDTO2.setAddress("ул. Пушкина, д. 10");
        responseDTO2.setDescription("Открытая парковка");
        responseDTO2.setPricePerHour(BigDecimal.valueOf(200.00));
        responseDTO2.setStatus(Status.BUSY);
        responseDTO2.setOwnerId(10);
        responseDTO2.setOwnerFullName("Иван Петров");

        responseList = List.of(responseDTO, responseDTO2);
    }

    @Test
    void getAllSpots_ShouldReturnList() throws Exception {
        when(parkingService.getAllSpots()).thenReturn(responseList);

        mockMvc.perform(get("/parking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].address").value("ул. Ленина, д. 5"))
                .andExpect(jsonPath("$[0].description").value("Крытая парковка с охраной"))
                .andExpect(jsonPath("$[0].pricePerHour").value(150.00))
                .andExpect(jsonPath("$[0].status").value("FREE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("BUSY"));
    }

    @Test
    void getSpotById_ShouldReturnSpot_WhenExists() throws Exception {
        when(parkingService.getSpotById(1)).thenReturn(responseDTO);

        mockMvc.perform(get("/parking/spot/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.address").value("ул. Ленина, д. 5"))
                .andExpect(jsonPath("$.description").value("Крытая парковка с охраной"))
                .andExpect(jsonPath("$.pricePerHour").value(150.00))
                .andExpect(jsonPath("$.status").value("FREE"))
                .andExpect(jsonPath("$.ownerId").value(10))
                .andExpect(jsonPath("$.ownerFullName").value("Иван Петров"));
    }

    @Test
    void getSpotById_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(parkingService.getSpotById(99)).thenThrow(new SpotNotFoundException("Spot not found"));

        mockMvc.perform(get("/parking/spot/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSpotByUser_ShouldReturnList_WhenUserExists() throws Exception {
        when(parkingService.getSpotByUser(10)).thenReturn(responseList);

        mockMvc.perform(get("/parking/user/{id}", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ownerId").value(10))
                .andExpect(jsonPath("$[1].ownerId").value(10));
    }

    @Test
    void getSpotByUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        when(parkingService.getSpotByUser(99)).thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/parking/user/{id}", 99))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSpot_ShouldReturnCreated_WhenValid() throws Exception {
        when(parkingService.createSpot(any(ParkingRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.address").value("ул. Ленина, д. 5"))
                .andExpect(jsonPath("$.description").value("Крытая парковка с охраной"))
                .andExpect(jsonPath("$.pricePerHour").value(150.00))
                .andExpect(jsonPath("$.status").value("FREE"))
                .andExpect(jsonPath("$.ownerId").value(10));
    }

    @Test
    void createSpot_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        ParkingRequestDTO invalidRequest = new ParkingRequestDTO(); // все поля null

        mockMvc.perform(post("/parking")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSpot_ShouldReturnOk_WhenValid() throws Exception {
        when(parkingService.updateSpot(eq(1), any(ParkingRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/parking/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.address").value("ул. Ленина, д. 5"))
                .andExpect(jsonPath("$.description").value("Крытая парковка с охраной"))
                .andExpect(jsonPath("$.pricePerHour").value(150.00));
    }

    @Test
    void updateSpot_ShouldReturnNotFound_WhenSpotNotExists() throws Exception {
        when(parkingService.updateSpot(eq(99), any(ParkingRequestDTO.class)))
                .thenThrow(new SpotNotFoundException("Spot not found"));

        mockMvc.perform(put("/parking/{id}", 99)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateSpot_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        ParkingRequestDTO invalidRequest = new ParkingRequestDTO(); // null fields

        mockMvc.perform(put("/parking/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteSpot_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        doNothing().when(parkingService).deleteSpot(1);

        mockMvc.perform(delete("/parking/{id}", 1))
                .andExpect(status().isNoContent());

        verify(parkingService).deleteSpot(1);
    }

    @Test
    void deleteSpot_ShouldReturnNotFound_WhenSpotNotExists() throws Exception {
        doThrow(new SpotNotFoundException("Spot not found")).when(parkingService).deleteSpot(99);

        mockMvc.perform(delete("/parking/{id}", 99))
                .andExpect(status().isNotFound());
    }
}
