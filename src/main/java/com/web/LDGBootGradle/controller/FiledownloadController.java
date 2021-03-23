package com.web.LDGBootGradle.controller;

import com.web.LDGBootGradle.model.UploadFile;
import com.web.LDGBootGradle.repository.FileRepository;
import com.web.LDGBootGradle.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/download")
public class FiledownloadController {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    private UploadFile uploadFile = new UploadFile();

    @GetMapping("/board/{fileId}")
    public ResponseEntity<Resource> fileDownload(@PathVariable("fileId") Long fileId) throws IOException {
        UploadFile uploadFile = fileService.getFile(fileId);
        Path path = Paths.get(uploadFile.getFileDownloadUri());
        Resource resource = new InputStreamResource(Files.newInputStream(path));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + uploadFile.getFileName() + "\"")
                .body(resource);
    }
}
