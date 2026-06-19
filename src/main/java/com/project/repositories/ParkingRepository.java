package com.project.repositories;

import com.project.models.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface ParkingRepository extends JpaRepository<ParkingSpot, Integer> {
    List<ParkingSpot> findByUserId(Integer userId);

}
