package com.mastercom.repository;

import com.mastercom.embeddableclasses.ScheduleDateID;
import com.mastercom.entity.VehicleToScheduleAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleToScheduleAssignmentRepository extends JpaRepository<VehicleToScheduleAssignment, ScheduleDateID> {
}
