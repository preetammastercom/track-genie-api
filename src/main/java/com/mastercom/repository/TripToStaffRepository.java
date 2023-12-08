package com.mastercom.repository;

import com.mastercom.embeddableclasses.TripStaffID;
import com.mastercom.entity.TripToStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripToStaffRepository extends JpaRepository<TripToStaff, TripStaffID> {
}
