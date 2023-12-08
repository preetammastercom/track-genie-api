package com.mastercom.repository;

import com.mastercom.embeddableclasses.TripStopID;
import com.mastercom.entity.TimeRequiredToReachAtStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeRequiredToReachAtStopRepository extends JpaRepository<TimeRequiredToReachAtStop, TripStopID> {
}
