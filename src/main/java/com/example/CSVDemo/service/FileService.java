package com.example.CSVDemo.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    boolean hasXlsxFormat(MultipartFile file);

    String processAndSaveData(MultipartFile file) throws Exception;

    void saveFile(MultipartFile file) throws Exception;

}
