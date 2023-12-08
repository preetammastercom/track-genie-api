package com.mastercom.repository;

import com.mastercom.entity.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
}
