package com.example.CSVDemo.repository;

import com.example.CSVDemo.entity.FileUploadInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileUploadInfoRepository extends JpaRepository<FileUploadInfo, Long> {
}
