package com.mwkim.projecthub.storage.service;

import com.mwkim.projecthub.storage.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private FileStorageProperties fileStorageProperties;

    // cleanup 옵션을 이용해 임시디렉토리 제거 안할 수 있다.
    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() {
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setUploadDir(tempDir.toString());
        fileStorageService = new FileStorageService(fileStorageProperties);
    }

    @Test
    @DisplayName("파일 저장 테스트")
    void testStoreFile() throws IOException {
        //given
        String fileName = "test.txt";
        String fileContent = "Hello, world";
        MockMultipartFile file = new MockMultipartFile("file", fileName, "text/plain", fileContent.getBytes());

        //when
        // 파일 저장
        String storedFileName = fileStorageService.storeFile(file);

        //then
        // 저장된 파일명이 올바른지 확인
        assertThat(storedFileName).matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}_test\\.txt$");
        // 파일이 실제로 저장된 위치 확인
        Path storedFilePath = tempDir.resolve(storedFileName);
        assertThat(storedFilePath).exists();
        // 저장된 파일의 내용이 원래 내용과 같은지 확인
        assertThat(Files.readString(storedFilePath)).isEqualTo(fileContent);
    }

    @Test
    @DisplayName("파일 제거 테스트")
    void testDeleteFile() throws IOException {
        // given
        String fileName = "test.txt";
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, "Test content");

        // when
        fileStorageService.deleteFile(fileName);

        //then
        assertThat(filePath).doesNotExist();
    }

    @Test
    @DisplayName("파일 미존재 시 제거 테스트")
    void testDeleteNonExistFile() {
        // given
        String nonExistFile = "non-exist.txt";

        // When & Then
        assertThatThrownBy(() -> fileStorageService.deleteFile(nonExistFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("파일을 삭제할 수 없습니다");
    }

    @Test
    @DisplayName("파일 경로조회 테스트")
    void testGetFilePath() {
        // given
        String fileName = "test.txt"; // 파일 존재와 상관없음

        // when
        Path filePath = fileStorageService.getFilePath(fileName);

        // then
        assertThat(filePath).isEqualTo(tempDir.resolve(fileName).normalize());
    }



}
