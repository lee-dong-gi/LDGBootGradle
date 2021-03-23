package com.web.LDGBootGradle.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UploadFile {
    @Id
    @GeneratedValue
    private Long uploadFileId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileDownloadUri;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private Long size;
}
