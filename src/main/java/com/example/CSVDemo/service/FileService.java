
    
package com.example.CSVDemo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public interface FileService {
    
   
    boolean hasXlsxFormat(MultipartFile file);

    String processAndSaveData(MultipartFile file) throws Exception;

 
    String saveFile(MultipartFile file) throws Exception;

   
    List<String> getTableNames();

    // List<Timestamp> getTableDates();

   
    List<Map<String, Object>> getTableInfo();
}


