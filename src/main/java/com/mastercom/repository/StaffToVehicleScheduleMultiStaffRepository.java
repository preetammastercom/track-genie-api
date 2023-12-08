package com.mastercom.repository;

import com.mastercom.embeddableclasses.VehicleScheduleDateStaffID;
import com.mastercom.entity.StaffToVehicleScheduleMultiStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StaffToVehicleScheduleMultiStaffRepository extends JpaRepository<StaffToVehicleScheduleMultiStaff, VehicleScheduleDateStaffID> {
}
