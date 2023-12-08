package com.mastercom.repository;

import com.mastercom.embeddableclasses.UserTypeOfJourneyID;
import com.mastercom.entity.PassengerToRouteID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerToRouteIDRepository extends JpaRepository<PassengerToRouteID, UserTypeOfJourneyID> {
}
