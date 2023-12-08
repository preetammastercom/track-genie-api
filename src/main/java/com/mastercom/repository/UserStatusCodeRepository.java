package com.mastercom.repository;

import com.mastercom.entity.UserStatusCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatusCodeRepository extends JpaRepository<UserStatusCode, Integer> {
}
