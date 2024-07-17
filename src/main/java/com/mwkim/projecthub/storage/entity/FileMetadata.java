package com.mwkim.projecthub.storage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 * 파일 메타데이터를 표현하는 엔티티 클래스
 *
 * 이 클래스는 업로드된 파일의 메타데이터를 저장하고 관리합니다.
 * JPA를 통해 데이터베이스와 매핑되며, 파일의 기본 정보와 저장 정보를 포함합니다.
 *
 * @note 모든 필드는 not null 제약조건을 가집니다. 필요시 nullable 필드 추가를 고려하세요.
 */

@Entity
@Getter @Setter @AllArgsConstructor
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename; // 원본 파일명

    @Column(nullable = false, unique = true)
    private String storedFilename; // 서버에 저장된 실제 파일명 (중복 방지를 위해 UUID 포함)

    @Column(nullable = false)
    private String contentType;  // 파일 MIME 타입 (파일 관리에 유용)

    @Column(nullable = false)
    private Long size;  // 파일 크기 (바이트)

    @Column(nullable = false)
    private String userId; // 파일 업로드 사용자 ID

    @Column(nullable = false)
    private LocalDateTime uploadDateTime; // 업로드 일시 immutable + thread-safe
}
