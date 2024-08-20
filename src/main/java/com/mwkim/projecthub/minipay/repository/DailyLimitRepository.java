package com.mwkim.projecthub.minipay.repository;

import com.mwkim.projecthub.minipay.entity.DailyLimit;
import com.mwkim.projecthub.minipay.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyLimitRepository extends JpaRepository<DailyLimit, Long> {
    Optional<DailyLimit> findByUserAndDate(User user, LocalDate date);
}
