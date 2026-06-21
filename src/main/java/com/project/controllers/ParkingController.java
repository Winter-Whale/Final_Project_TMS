package com.project.controllers;

import com.project.models.dto.Parking.ParkingRequestDTO;
import com.project.models.dto.Parking.ParkingResponseDTO;
import com.project.services.ParkingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Currency;
import java.util.List;

@RestController
@RequestMapping("/parking")
public class ParkingController {
    private final ParkingService parkingService;
    @Autowired
    public  ParkingController(ParkingService parkingService){this.parkingService = parkingService;}

    @GetMapping
    public ResponseEntity<List<ParkingResponseDTO>> getAllSpots(){
        return new ResponseEntity<>(parkingService.getAllSpots(), HttpStatus.OK);
    }

    @GetMapping("/spot/{id}")
    public ResponseEntity<ParkingResponseDTO> getSpotById(@PathVariable Integer id){
        ParkingResponseDTO responseDTO = parkingService.getSpotById(id);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<List<ParkingResponseDTO>> getSpotByUser(@PathVariable Integer id){
       return  new ResponseEntity<>(parkingService.getSpotByUser(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ParkingResponseDTO> createSpot(@RequestBody @Valid ParkingRequestDTO requestDTO) {
        ParkingResponseDTO createSpot = parkingService.createSpot(requestDTO);
        return new ResponseEntity<>(createSpot, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public  ResponseEntity<ParkingResponseDTO> updateSpot(@PathVariable Integer id, @RequestBody @Valid ParkingRequestDTO requestDTO){
        ParkingResponseDTO update = parkingService.updateSpot(id ,requestDTO);
        return new  ResponseEntity<>(update, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <HttpStatus> deleteSpot(@PathVariable Integer id){
        parkingService.deleteSpot(id);
        return  new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
