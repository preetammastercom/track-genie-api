package com.mastercom.repository;

import com.mastercom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findByUserPhoneNumber(long userPhoneNumber);
    User findByEmail(String email);
    User findByUserUniqueKey(String userUniqueKey);
    
}
