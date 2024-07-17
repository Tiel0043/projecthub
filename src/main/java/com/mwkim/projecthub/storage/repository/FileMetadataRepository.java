package com.mwkim.projecthub.storage.repository;

import com.mwkim.projecthub.storage.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    Optional<FileMetadata> findByStoredFilenameAAndUserId(String storedFileName, String userId); // PK 노출 방지
}
