package com.mwkim.projecthub.storage.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


/*
    @ConfigurationProperties의 장단점
    1. 프로퍼티 값을 객체로 반환해 타입 오류 방지
    2. 관련 설정 중앙화
    3. 유연하게 쉽게 설정을 바꿀 수 있다.
 */
@Getter @Setter
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    private String uploadDir; // 저장할 디렉토리

}
