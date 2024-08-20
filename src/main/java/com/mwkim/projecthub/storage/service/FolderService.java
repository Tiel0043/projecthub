package com.mwkim.projecthub.storage.service;

import com.mwkim.projecthub.storage.entity.Folder;
import com.mwkim.projecthub.storage.exception.FolderAlreadyExistsException;
import com.mwkim.projecthub.storage.exception.FolderNotFoundException;
import com.mwkim.projecthub.storage.repository.FileMetadataRepository;
import com.mwkim.projecthub.storage.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FolderService {

    private final FolderRepository folderRepository;
    private final FileMetadataRepository fileMetadataRepository;

    // Folder로 리턴 변경필요
    public void createFolder(String folderPath, String userId) {

        // 기존 폴더가 있으면 생성 x
        if (folderRepository.findByPathAndUserId(folderPath, userId).isPresent()) {
            throw new FolderAlreadyExistsException("Folder already Exist: " + folderPath);
        }

//        Folder folder = new Folder();
//        folder.setName(getNameFromPath(folderPath)); // Path : c:/users/minwoo 라면 minwoo만 파싱해 전달
//        folder.setPath(folderPath);
//        folder.setUserId(userId);
//
////        Folder savedFolder = folderRepository.save();
//        return savedFolder;
    }

    public Folder renameFolder(String oldPath, String newName, String userId) {
        Folder folder = folderRepository.findByPathAndUserId(oldPath, userId)
                .orElseThrow(() -> new FolderNotFoundException("Folder Not Found : " + oldPath));

        String newPath = getParentPath(oldPath) + "/" + newName;
        if (folderRepository.findByPathAndUserId(newPath, userId).isPresent()) {
            throw new FolderAlreadyExistsException("Folder already exists: " + newPath);
        }

        folder.setName(newName);
        folder.setPath(newPath);

        // 폴더 내의 모든 파일(서브폴더 포함)들의 경로업데이트

        // 1. 서브폴더 업데이트
        // c:/user/mwkim/subfolders   -> c : /user/change/subfolders -> new path : /user/change + (각파일이름)
        List<Folder> subFolders = folderRepository.findByPathStartingWithAndUserId(oldPath + "/", userId);
        for (Folder subFolder : subFolders) {
            subFolder.setPath(newPath + subFolder.getPath().substring(oldPath.length()));
        }

        // 파일 경로 업데이트
//        fileMetadataRepository.fin
        return folder;
    }


    // 파일 경로의 파일이름을 추출하는 메소드
    // path 경로에서 마지막 / 이후의 값을 추출한다.
    private String getNameFromPath(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    // 파일 이름을 제외한 경로를 추출하는 메소드
    private String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

}
