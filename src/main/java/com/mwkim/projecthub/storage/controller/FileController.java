package com.mwkim.projecthub.storage.controller;

import com.mwkim.projecthub.storage.entity.FileMetadata;
import com.mwkim.projecthub.storage.service.FileService;
import com.mwkim.projecthub.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FileStorageService fileStorageService;

    // 파일 업로드 컨트롤러
    @PostMapping("/upload")
    public ResponseEntity<FileMetadata> uploadFile(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("userId") String userId) {
        FileMetadata fileMetadata = fileService.uploadFile(file, userId);
        return ResponseEntity.ok(fileMetadata);
    }

    // 파일 제거 컨트롤러
    @DeleteMapping("/{storedFileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable String storedFileName,
                                           @RequestParam("userId") String userId) {
        fileService.deleteFile(storedFileName, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{storedFileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String storedFileName,
                                                 @RequestParam("userId") String userId) {
        FileMetadata fileMetadata = fileService.getFileMetadata(storedFileName, userId);
        Path filePath = fileStorageService.getFilePath(storedFileName);

        try {
            Resource urlResource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType()
        }

    }
}
