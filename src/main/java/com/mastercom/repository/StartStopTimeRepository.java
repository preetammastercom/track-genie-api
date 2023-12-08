package com.mastercom.repository;

import com.mastercom.entity.StartStopTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StartStopTimeRepository extends JpaRepository<StartStopTime, Integer> {
}
