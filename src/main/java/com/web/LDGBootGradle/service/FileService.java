package com.web.LDGBootGradle.service;


import com.web.LDGBootGradle.model.UploadFile;
import com.web.LDGBootGradle.repository.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public UploadFile getFile(Long id){return fileRepository.findByUploadFileId(id);}

    public UploadFile save(UploadFile uploadFile){
        return fileRepository.save(uploadFile);
    }
}
