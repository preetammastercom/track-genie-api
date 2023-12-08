package com.mastercom.repository;

import com.mastercom.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StopRepository extends JpaRepository<Stop, Integer> {
}
