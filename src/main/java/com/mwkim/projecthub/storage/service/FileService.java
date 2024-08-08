package com.mwkim.projecthub.storage.service;

import com.mwkim.projecthub.storage.entity.FileMetadata;
import com.mwkim.projecthub.storage.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 파일 관련 비즈니스 로직을 처리하는 서비스 클래스.
 * 이 클래스는 파일 업로드, 삭제, 메타데이터 조회 등의 작업을 처리합니다.
 *
 * @note 트랜잭션 처리가 필요한 경우 @Transactional 어노테이션을 적절히 사용하세요.
 * @note 대용량 데이터 처리 시 페이징이나 청크 처리를 고려해야 할 수 있습니다.
 */

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final FileStorageService fileStorageService;

    /**
     * 파일을 업로드하고 메타데이터를 저장합니다.
     *
     * @param file   업로드할 multipart 객체
     * @param userId 파일을 업로드하는 사용자의 ID
     * @return 저장된 파일의 메타데이터
     */
    public FileMetadata uploadFile(MultipartFile file, String userId) {
        String storedFileName = fileStorageService.storeFile(file); // 파일 업로드

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename(file.getOriginalFilename()); // 메타데이터는 DB에 저장되기에 정규화 필요X (사용자 구분)
        fileMetadata.setStoredFilename(storedFileName);
        fileMetadata.setContentType(file.getContentType());
        fileMetadata.setSize(file.getSize());
        fileMetadata.setUserId(userId);
        fileMetadata.setUploadDateTime(LocalDateTime.now());

        FileMetadata savedMetadata = fileMetadataRepository.save(fileMetadata);
        return savedMetadata; // 저장된 메타데이터 반환
    }

    /**
     * 파일을 삭제하고 관련 메타데이터를 제거합니다.
     *
     * @param storedFileName 삭제할 파일의 저장된 이름
     * @param userId         파일을 삭제하려는 사용자의 ID
     * @throws FileNotFoundException 파일이 존재하지 않거나 사용자에게 권한이 없는 경우
     */
    public void deleteFile(String storedFileName, String userId) throws FileNotFoundException{
        // 메타데이터 조회
        FileMetadata fileMetadata = getFileMetadata(storedFileName, userId);

        // 파일 삭제
        fileStorageService.deleteFile(storedFileName);

        // 파일 메타데이터 삭제
        fileMetadataRepository.delete(fileMetadata);
    }

    public FileMetadata getFileMetadata(String storedFileName, String userId) throws FileNotFoundException{

        Optional<FileMetadata> result = fileMetadataRepository.findByStoredFilenameAndUserId(storedFileName, userId);

        return result.orElseThrow(() -> {
            return new FileNotFoundException("File not found with name " + storedFileName);
        });
    }

}
