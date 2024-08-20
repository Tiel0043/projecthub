package com.mwkim.projecthub.storage.repository;

import com.mwkim.projecthub.storage.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    Optional<Folder> findByPathAndUserId(String path, String userId);
    List<Folder> findByPathStartingWithAndUserId(String pathPrefix, String userid);
}
