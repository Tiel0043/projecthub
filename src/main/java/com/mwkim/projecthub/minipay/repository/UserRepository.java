package com.mwkim.projecthub.minipay.repository;

import com.mwkim.projecthub.minipay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
