package com.project.services;

import com.project.exceptions.RegistrationException;
import com.project.exceptions.SpotNotFoundException;
import com.project.exceptions.UserNotFoundException;
import com.project.exceptions.YouAreNotOwnerException;
import com.project.models.ParkingSpot;
import com.project.models.Role;
import com.project.models.Status;
import com.project.models.User;
import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.repositories.ParkingRepository;
import com.project.repositories.UserRepository;
import com.project.util.ParkingSpotMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    @Mock
    private ParkingRepository parkingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ParkingSpotMapper parkingSpotMapper;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ParkingService parkingService;

    private final Integer SPOT_ID = 1;
    private final Integer OWNER_ID = 10;
    private final Integer CURRENT_USER_ID = 10;
    private final String ADDRESS = "Test Address";
    private final String DESCRIPTION = "Test Description";
    private final BigDecimal PRICE = BigDecimal.valueOf(10.0);

    private User owner;
    private User currentUser;
    private ParkingSpot spot;
    private ParkingRequestDTO requestDTO;
    private ParkingResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(OWNER_ID);
        owner.setFullName("Owner");

        currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setFullName("Current User");

        spot = new ParkingSpot();
        spot.setId(SPOT_ID);
        spot.setAddress(ADDRESS);
        spot.setDescription(DESCRIPTION);
        spot.setPricePerHour(PRICE);
        spot.setStatus(Status.FREE);
        spot.setUser(owner);

        requestDTO = new ParkingRequestDTO();
        requestDTO.setOwnerId(OWNER_ID);
        requestDTO.setAddress(ADDRESS);
        requestDTO.setDescription(DESCRIPTION);
        requestDTO.setPricePerHour(PRICE);
        requestDTO.setStatus(Status.FREE);

        responseDTO = new ParkingResponseDTO();
        responseDTO.setId(SPOT_ID);
        responseDTO.setAddress(ADDRESS);
        responseDTO.setDescription(DESCRIPTION);
        responseDTO.setPricePerHour(PRICE);
        responseDTO.setStatus(Status.FREE);
        responseDTO.setOwnerId(OWNER_ID);
    }

    @Test
    void shouldCreateSpot_whenUserIsOwnerAndCreatesForHimself() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.hasRole(Role.OWNER)).thenReturn(true);
        when(parkingSpotMapper.mapFromParkingRequestDTOToParkingSpot(requestDTO)).thenReturn(spot);
        when(parkingRepository.save(any(ParkingSpot.class))).thenReturn(spot);
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);

        ParkingResponseDTO result = parkingService.createSpot(requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(parkingRepository).save(spot);
        verify(parkingSpotMapper).mapFromParkingRequestDTOToParkingSpot(requestDTO);
        verify(parkingSpotMapper).mapFromParkingSpotToParkingResponseDTO(spot);
    }

    @Test
    void shouldThrowRegistrationException_whenUserNotFound() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.createSpot(requestDTO))
                .isInstanceOf(RegistrationException.class)
                .hasMessage("Registration error, please check the data is correct");
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void shouldThrowYouAreNotOwnerException_whenOwnerTriesToCreateForAnotherUser() {
        User anotherUser = new User();
        anotherUser.setId(99);
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(anotherUser));
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(currentUserService.hasRole(Role.OWNER)).thenReturn(true);

        assertThatThrownBy(() -> parkingService.createSpot(requestDTO))
                .isInstanceOf(YouAreNotOwnerException.class)
                .hasMessage("You can only create spots for yourself");
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void shouldGetAllSpotsAndReleaseExpired() {
        List<ParkingSpot> spots = List.of(spot);
        when(parkingRepository.findAll()).thenReturn(spots);
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);
        when(parkingRepository.releaseExpiredSpots(eq(Status.FREE), eq(Status.BUSY), any(LocalDateTime.class)))
                .thenReturn(2);

        List<ParkingResponseDTO> result = parkingService.getAllSpots();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
        verify(parkingRepository).releaseExpiredSpots(eq(Status.FREE), eq(Status.BUSY), any(LocalDateTime.class));
        verify(parkingRepository).findAll();
    }

    @Test
    void shouldGetSpotById_whenExists() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);

        ParkingResponseDTO result = parkingService.getSpotById(SPOT_ID);

        assertThat(result).isEqualTo(responseDTO);
        verify(parkingRepository).findById(SPOT_ID);
    }

    @Test
    void shouldThrowSpotNotFoundException_whenSpotNotFoundById() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.getSpotById(SPOT_ID))
                .isInstanceOf(SpotNotFoundException.class)
                .hasMessage("Spot not found with id: " + SPOT_ID);
        verify(parkingRepository).findById(SPOT_ID);
    }

    @Test
    void shouldGetSpotsByUserId_whenUserExists() {
        List<ParkingSpot> spots = List.of(spot);
        when(userRepository.existsById(OWNER_ID)).thenReturn(true);
        when(parkingRepository.findByUserId(OWNER_ID)).thenReturn(spots);
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);

        List<ParkingResponseDTO> result = parkingService.getSpotByUser(OWNER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(responseDTO);
        verify(userRepository).existsById(OWNER_ID);
        verify(parkingRepository).findByUserId(OWNER_ID);
    }

    @Test
    void shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.existsById(OWNER_ID)).thenReturn(false);

        assertThatThrownBy(() -> parkingService.getSpotByUser(OWNER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: " + OWNER_ID);
        verify(parkingRepository, never()).findByUserId(anyInt());
    }

    @Test
    void shouldUpdateSpot_whenUserIsAdmin() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(true);
        when(parkingRepository.save(any(ParkingSpot.class))).thenReturn(spot);
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);

        ParkingResponseDTO result = parkingService.updateSpot(SPOT_ID, requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        ArgumentCaptor<ParkingSpot> captor = ArgumentCaptor.forClass(ParkingSpot.class);
        verify(parkingRepository).save(captor.capture());
        ParkingSpot updated = captor.getValue();
        assertThat(updated.getAddress()).isEqualTo(ADDRESS);
        assertThat(updated.getDescription()).isEqualTo(DESCRIPTION);
        assertThat(updated.getPricePerHour()).isEqualTo(PRICE);
        assertThat(updated.getStatus()).isEqualTo(Status.FREE);
        verify(currentUserService).hasRole(Role.ADMIN);
        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    void shouldUpdateSpot_whenUserIsOwner() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);
        when(parkingRepository.save(any(ParkingSpot.class))).thenReturn(spot);
        when(parkingSpotMapper.mapFromParkingSpotToParkingResponseDTO(spot)).thenReturn(responseDTO);

        ParkingResponseDTO result = parkingService.updateSpot(SPOT_ID, requestDTO);

        assertThat(result).isEqualTo(responseDTO);
        verify(parkingRepository).save(spot);
        verify(currentUserService).hasRole(Role.ADMIN);
        verify(currentUserService).getCurrentUser();
    }

    @Test
    void shouldThrowSpotNotFoundException_whenUpdatingNonExistingSpot() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.updateSpot(SPOT_ID, requestDTO))
                .isInstanceOf(SpotNotFoundException.class)
                .hasMessage("Spot not found with id: " + SPOT_ID);
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void shouldThrowYouAreNotOwnerException_whenNonAdminTriesToUpdateOthersSpot() {
        User anotherUser = new User();
        anotherUser.setId(99);
        spot.setUser(anotherUser);

        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> parkingService.updateSpot(SPOT_ID, requestDTO))
                .isInstanceOf(YouAreNotOwnerException.class)
                .hasMessage("You are not the owner of this parking spot");
        verify(parkingRepository, never()).save(any());
    }

    @Test
    void shouldDeleteSpot_whenUserIsAdmin() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(true);

        parkingService.deleteSpot(SPOT_ID);

        verify(parkingRepository).delete(spot);
        verify(currentUserService).hasRole(Role.ADMIN);
        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    void shouldDeleteSpot_whenUserIsOwner() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        parkingService.deleteSpot(SPOT_ID);

        verify(parkingRepository).delete(spot);
        verify(currentUserService).hasRole(Role.ADMIN);
        verify(currentUserService).getCurrentUser();
    }

    @Test
    void shouldThrowSpotNotFoundException_whenDeletingNonExistingSpot() {
        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> parkingService.deleteSpot(SPOT_ID))
                .isInstanceOf(SpotNotFoundException.class)
                .hasMessage("Spot not found with id: " + SPOT_ID);
        verify(parkingRepository, never()).delete(any());
    }

    @Test
    void shouldThrowYouAreNotOwnerException_whenNonAdminTriesToDeleteOthersSpot() {
        User anotherUser = new User();
        anotherUser.setId(99);
        spot.setUser(anotherUser);

        when(parkingRepository.findById(SPOT_ID)).thenReturn(Optional.of(spot));
        when(currentUserService.hasRole(Role.ADMIN)).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(currentUser);

        assertThatThrownBy(() -> parkingService.deleteSpot(SPOT_ID))
                .isInstanceOf(YouAreNotOwnerException.class)
                .hasMessage("You are not the owner of this parking spot");
        verify(parkingRepository, never()).delete(any());
    }
}