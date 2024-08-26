package com.mwkim.projecthub.minipay.repository;

import com.mwkim.projecthub.minipay.entity.Settlement;
import com.mwkim.projecthub.minipay.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
}
