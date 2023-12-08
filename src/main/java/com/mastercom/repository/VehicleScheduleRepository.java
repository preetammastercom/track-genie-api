package com.mastercom.repository;

import com.mastercom.entity.VehicleSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleScheduleRepository extends JpaRepository<VehicleSchedule, Integer> {
}
