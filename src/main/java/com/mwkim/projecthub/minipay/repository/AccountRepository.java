package com.mwkim.projecthub.minipay.repository;

import com.mwkim.projecthub.minipay.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
}
