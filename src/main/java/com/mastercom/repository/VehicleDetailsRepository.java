package com.mastercom.repository;

import com.mastercom.entity.VehicleDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleDetailsRepository extends JpaRepository<VehicleDetails, Integer> {
}
