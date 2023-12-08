package com.mastercom.repository;

import com.mastercom.embeddableclasses.TripUser;
import com.mastercom.entity.PassengerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerStatusRepository extends JpaRepository<PassengerStatus, TripUser> {
}
