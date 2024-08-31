package com.mwkim.projecthub.minipay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @Builder
    public User(String name) {
        this.name = name;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
        account.addUser(this);
    }

    // factory-method
    public static User createUser(String name) {
        return User.builder()
                .name(name)
                .build();
    }
}
