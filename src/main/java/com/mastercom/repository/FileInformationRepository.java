package com.mastercom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mastercom.entity.FileInformation;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInformationRepository extends JpaRepository<FileInformation, Long>{

}
