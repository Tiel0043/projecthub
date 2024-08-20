package com.mwkim.projecthub.storage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "folders")
@Getter
@Setter
@AllArgsConstructor
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //이 어노테이션은 엔티티가 데이터베이스에 처음 저장되기 직전에 실행될 메서드를 지정합니다.
    @PrePersist
    private void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    // 이 어노테이션은 엔티티가 데이터베이스에서 업데이트되기 직전에 실행될 메서드를 지정합니다.
    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
