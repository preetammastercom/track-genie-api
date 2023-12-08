package com.mastercom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mastercom.entity.Notification;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>{

}
