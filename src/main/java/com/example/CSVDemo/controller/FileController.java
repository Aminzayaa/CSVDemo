package com.example.CSVDemo.controller;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.hibernate.boot.archive.internal.ByteArrayInputStreamAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.CSVDemo.response.ResponseMessage;
import com.example.CSVDemo.service.FileService;


@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileService service;

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        if (service.hasCsvFormat(file)) {
            try {
                service.processAndSaveData(file);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseMessage("Uploaded the file successfully: " + file.getOriginalFilename()));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                        .body(new ResponseMessage("Failed to process file: " + e.getMessage()));
            }
        }
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage("Please upload a CSV file"));
    }

    
}
