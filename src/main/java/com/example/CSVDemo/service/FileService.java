package com.example.CSVDemo.service;

import org.hibernate.boot.archive.internal.ByteArrayInputStreamAccess;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    boolean hasCsvFormat(MultipartFile file);
    
    // Make sure the method signature matches exactly with the implementation
    String processAndSaveData(MultipartFile file) throws Exception;

    // 
}
