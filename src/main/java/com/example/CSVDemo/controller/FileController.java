package com.example.CSVDemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.CSVDemo.service.FileService;


@Controller
public class FileController {

    @Autowired
    private FileService fileService;
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
    try {
        fileService.saveFile(file); // Save the file
        redirectAttributes.addFlashAttribute("message", "File uploaded successfully!");
        redirectAttributes.addFlashAttribute("alertClass", "alert-success");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("message", "Failed to upload the file: " + e.getMessage());
        redirectAttributes.addFlashAttribute("alertClass", "alert-danger");
    }
    return "redirect:/uploadPage"; // Redirect to the upload page
}

    @GetMapping("/uploadPage")
    public String uploadPage() {
        return "uploadFile";
    } 
}