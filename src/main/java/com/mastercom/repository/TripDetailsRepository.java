package com.mastercom.repository;

import com.mastercom.entity.TripDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripDetailsRepository extends JpaRepository<TripDetails, Integer> {
}
