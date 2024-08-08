package com.mwkim.projecthub.storage.controller;

import com.mwkim.projecthub.storage.entity.FileMetadata;
import com.mwkim.projecthub.storage.service.FileService;
import com.mwkim.projecthub.storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
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
        System.out.println("file = " + file.getOriginalFilename());
        FileMetadata fileMetadata = fileService.uploadFile(file, userId);
        return ResponseEntity.ok(fileMetadata);
    }

    // 파일 제거 컨트롤러
    @DeleteMapping("/{storedFileName}")
    public ResponseEntity<Void> deleteFile(@PathVariable("storedFileName") String storedFileName,
                                           @RequestParam("userId") String userId) throws FileNotFoundException{
        System.out.println("storedFileName = " + storedFileName);
        fileService.deleteFile(storedFileName, userId);
        return ResponseEntity.ok().build();
    }

    // 파일 다운로드 컨트롤러
    @GetMapping("/{storedFileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable("storedFileName") String storedFileName,
                                                 @RequestParam("userId") String userId) throws FileNotFoundException {

        FileMetadata fileMetadata = fileService.getFileMetadata(storedFileName, userId);

        if (!fileMetadata.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        Path filePath = fileStorageService.getFilePath(storedFileName);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileMetadata.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드 에러", e);
        }

    }
}
