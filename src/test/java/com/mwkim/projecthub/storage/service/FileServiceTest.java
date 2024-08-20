package com.mwkim.projecthub.storage.service;

import com.mwkim.projecthub.storage.entity.FileMetadata;
import com.mwkim.projecthub.storage.repository.FileMetadataRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private FileStorageService fileStorageService;


    @InjectMocks
    private FileService fileService; // Mockito가 모의 객체를 주입한 FileService 인스턴스 생성

    private static final String USER_ID = "testUser";
    private static final String STORED_FILE_NAME = "stored_test_file.txt";

    @Test
    @DisplayName("파일 업로드 테스트")
    void uploadFile_Success() {

        // given : 준비
        MockMultipartFile file = new MockMultipartFile("file", "test_file.txt", "text/plain", "test content".getBytes());
        // fileStorageService.storeFile() 메서드가 호출되면 STORED_FILE_NAME을 반환하도록 설정
        when(fileStorageService.storeFile(any(MultipartFile.class))).thenReturn(STORED_FILE_NAME);

        // fileMetadataRepository.save() 메서드가 호출되면 전달받은 인자를 그대로 반환하도록 설정합니다.
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenAnswer(i -> i.getArguments()[0]);

        // when: 테스트하려는 메서드를 실행
        FileMetadata result = fileService.uploadFile(file, USER_ID);

        // then : 검증
        assertNotNull(result);
        assertEquals("test_file.txt", result.getFilename());
        assertEquals(STORED_FILE_NAME, result.getStoredFilename());
        assertEquals(USER_ID, result.getUserId());

        // fileStorageService.storeFile() 메소드가 한 번 호출 되었는지 확인
        verify(fileStorageService).storeFile(file);
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    @DisplayName("파일 삭제 테스트 - 파일을 찾을 수 없음")
    void deleteFile_FileNotFound() {
        // given
        when(fileMetadataRepository.findByStoredFilenameAndUserId(STORED_FILE_NAME, USER_ID))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(FileNotFoundException.class, () -> fileService.deleteFile(STORED_FILE_NAME, USER_ID));
    }

    @Test
    @DisplayName("파일 삭제 테스트 - 성공")
    void deleteFile_Success() {
        // given
        FileMetadata metadata = new FileMetadata();
        metadata.setUserId(USER_ID);
        metadata.setStoredFilename(STORED_FILE_NAME);
        when(fileMetadataRepository.findByStoredFilenameAndUserId(STORED_FILE_NAME, USER_ID))
                .thenReturn(Optional.of(metadata));

        // when
        assertDoesNotThrow(() -> fileService.deleteFile(STORED_FILE_NAME, USER_ID));

        // then
        verify(fileStorageService).deleteFile(STORED_FILE_NAME);
        verify(fileMetadataRepository).delete(metadata);
    }

    @Test
    @DisplayName("메타데이터 조회 테스트")
    void getFileMetadata_Success() {
        // given
        FileMetadata mockMetadata = new FileMetadata();
        mockMetadata.setUserId(USER_ID);
        mockMetadata.setStoredFilename(STORED_FILE_NAME);
        System.out.println("Mocking repository method...");
        when(fileMetadataRepository.findByStoredFilenameAndUserId(eq(STORED_FILE_NAME), eq(USER_ID)))
                .thenReturn(Optional.of(mockMetadata));

        System.out.println("Mocking completed.");

        // when
        FileMetadata result;
        try {
            result = fileService.getFileMetadata(STORED_FILE_NAME, USER_ID);
            System.out.println("result = " + result);
        } catch (FileNotFoundException e) {
            fail("FileNotFoundException should not be thrown");
            return;
        }

        // then
        assertNotNull(result);
        assertEquals(mockMetadata, result);
        assertEquals(USER_ID, result.getUserId());
        System.out.println("result.getUserId() = " + result.getUserId());
        System.out.println("result = " + result.getStoredFilename());
        assertEquals(STORED_FILE_NAME, result.getStoredFilename());
    }
}
