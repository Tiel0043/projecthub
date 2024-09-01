package com.mwkim.projecthub.minipay.repository;

import com.mwkim.projecthub.minipay.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
