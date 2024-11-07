package com.example.CSVDemo.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileService {
    boolean hasXlsxFormat(MultipartFile file);

    String processAndSaveData(MultipartFile file) throws Exception;

    void saveFile(MultipartFile file) throws Exception;

    List<String> getTableNames(); // method to get table names
}
