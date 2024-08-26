package com.mwkim.projecthub.minipay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    public List<Account> getAccounts() {
        return accounts;
    }

    // 연관관계 편의 메소드
    public void addAccount(Account account) {
        this.accounts.add(account);
        account.addUser(this);
    }

    @Builder
    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
