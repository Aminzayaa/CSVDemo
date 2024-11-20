package com.example.CSVDemo.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "file_upload_info")
public class FileUploadInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "upload_date")
    private Timestamp uploadDate;

    public FileUploadInfo() {
    }

    public FileUploadInfo(String tableName, Timestamp uploadDate) {
        this.tableName = tableName;
        this.uploadDate = uploadDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }
}
