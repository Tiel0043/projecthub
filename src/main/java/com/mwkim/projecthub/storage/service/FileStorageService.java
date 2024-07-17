package com.mwkim.projecthub.storage.service;

import com.mwkim.projecthub.storage.config.FileStorageProperties;
import com.mwkim.projecthub.storage.entity.FileMetadata;
import com.mwkim.projecthub.storage.repository.FileMetadataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 *  파일 저장소 관련 작업을 처리하는 서비스 클래스.
 *
 *  이 클래스는 실제 파일 시스템에 대한 작업을 담당합니다.
 *  파일의 물리적 저장, 삭제, 경로 조회 등의 기능을 제공합니다.
 *
 * @note 파일 시스템 작업은 예외처리가 중요합니다. 모든 I/O 작업에서 예외를 적절히 처리해주세요.
 * @note 대용량 파일 처리 시 메모리 사용에 주의가 필요할 수 있습니다.
 */

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {

        // getUploadDir()을 통한 디렉토리 경로를 가져와 절대경로로 변환 후 정규화(불필요한 .이나 .. 제거)
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation); // 디렉토리가 없으면 생성
        } catch (Exception ex) {
            throw new RuntimeException("파일을 저장할 디렉토리를 생성할 수 없습니다", ex);
        }
    }

    /**
     * 파일을 저장소에 저장합니다.
     *
     * @param file 저장할 MultipartFile 객체
     * @return 저장된 파일의 이름 (UUID가 포함된 고유의 이름)
     * @throws RuntimeException 파일 저장 중 오류 발생 시
     */
    public String storeFile(MultipartFile file) {

        // 파일명을 정규화하는 이유 : Directory Travarsal
        /*
         ../../etc/shadow 같은 파일명이 들어온다면
         new File("/var/www/uploads/" + fileName);을 다운받는다고 한다면
         서버는 /var/www/uploads/../../../../etc/shadow 경로의 파일을 읽어 클라이언트에게 반환한다.
         이는 /etc/shadow 파일에 접근해 시스템의 비밀번호 해시를 노출시킬 수 있다.
         */
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID().toString() + "_" + fileName; // 문자열 합치기..? 수정할지도

        try {
            Path targetLocation = this.fileStorageLocation.resolve(storedFileName); // 디렉토리 경로 + 파일명 ex) dir/file.md
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // 입력 스트림을 이용해 파일을 targetLocation에 복사, 이미 존재하는 파일이라면 덮어쓰기
            return storedFileName;
        } catch (IOException ex) {
            throw new RuntimeException(fileName + "파일을 저장할 수 없습니다. 다시 시도해주세요", ex);
        }
    }

    /**
     * 저장된 파일을 삭제합니다.
     *
     * @param storedFileName 삭제할 파일의 저장된 이름
     * @throws RuntimeException 파일 삭제 중 오류 발생 시
     */
    public void deleteFile(String storedFileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFileName).normalize();
            Files.delete(filePath);
        } catch (IOException ex) {
            throw new RuntimeException(storedFileName + "파일을 삭제할 수 없습니다.")
        }
    }

    /**
     * 저장된 파일의 경로를 반환합니다.
     *
     * @param storedFileName 조회할 파일의 저장된 이름
     * @return 파일의 전체 경로
     */
    public Path getFilePath(String storedFileName) {
        return this.fileStorageLocation.resolve(storedFileName).normalize();
    }

}
