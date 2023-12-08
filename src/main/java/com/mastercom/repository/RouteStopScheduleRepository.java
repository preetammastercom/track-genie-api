package com.mastercom.repository;

import com.mastercom.entity.RouteStopSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteStopScheduleRepository extends JpaRepository<RouteStopSchedule, Integer> {
}
