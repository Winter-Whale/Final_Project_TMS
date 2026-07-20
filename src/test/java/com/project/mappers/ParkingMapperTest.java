package com.project.mappers;

import com.project.models.ParkingSpot;
import com.project.models.Status;
import com.project.models.User;
import com.project.models.dto.parking.ParkingRequestDTO;
import com.project.models.dto.parking.ParkingResponseDTO;
import com.project.util.ParkingSpotMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ParkingMapperTest {

    private ParkingSpotMapper mapper;

    private final Integer SPOT_ID = 1;
    private final String ADDRESS = "Test Address";
    private final String DESCRIPTION = "Test Description";
    private final BigDecimal PRICE = BigDecimal.valueOf(10.0);
    private final Status STATUS = Status.FREE;
    private final Integer OWNER_ID = 100;
    private final String OWNER_NAME = "John Doe";

    private ParkingRequestDTO requestDTO;
    private ParkingSpot spot;
    private User owner;

    @BeforeEach
    void setUp() {
        mapper = new ParkingSpotMapper();

        requestDTO = new ParkingRequestDTO();
        requestDTO.setAddress(ADDRESS);
        requestDTO.setDescription(DESCRIPTION);
        requestDTO.setPricePerHour(PRICE);
        requestDTO.setStatus(STATUS);

        owner = new User();
        owner.setId(OWNER_ID);
        owner.setFullName(OWNER_NAME);

        spot = new ParkingSpot();
        spot.setId(SPOT_ID);
        spot.setAddress(ADDRESS);
        spot.setDescription(DESCRIPTION);
        spot.setPricePerHour(PRICE);
        spot.setStatus(STATUS);
        spot.setUser(owner);
    }

    @Test
    void shouldMapRequestDTOToParkingSpotCorrectly() {
        ParkingSpot result = mapper.mapFromParkingRequestDTOToParkingSpot(requestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(0); // id не устанавливается, примитив int -> 0
        assertThat(result.getAddress()).isEqualTo(ADDRESS);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getPricePerHour()).isEqualTo(PRICE);
        assertThat(result.getStatus()).isEqualTo(STATUS);
        assertThat(result.getUser()).isNull(); // user не устанавливается
    }

    @Test
    void shouldThrowNullPointerException_whenRequestDTOIsNull() {
        assertThatThrownBy(() -> mapper.mapFromParkingRequestDTOToParkingSpot(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMapParkingSpotToResponseDTOCorrectly() {
        ParkingResponseDTO result = mapper.mapFromParkingSpotToParkingResponseDTO(spot);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(SPOT_ID);
        assertThat(result.getAddress()).isEqualTo(ADDRESS);
        assertThat(result.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(result.getPricePerHour()).isEqualTo(PRICE);
        assertThat(result.getStatus()).isEqualTo(STATUS);
        assertThat(result.getOwnerId()).isEqualTo(OWNER_ID);
        assertThat(result.getOwnerFullName()).isEqualTo(OWNER_NAME);
    }

    @Test
    void shouldThrowNullPointerException_whenSpotIsNull() {
        assertThatThrownBy(() -> mapper.mapFromParkingSpotToParkingResponseDTO(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowNullPointerException_whenSpotHasNullUser() {
        spot.setUser(null);

        assertThatThrownBy(() -> mapper.mapFromParkingSpotToParkingResponseDTO(spot))
                .isInstanceOf(NullPointerException.class);
    }
}
