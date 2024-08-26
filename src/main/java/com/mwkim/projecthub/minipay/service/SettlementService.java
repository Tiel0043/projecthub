package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Settlement;
import com.mwkim.projecthub.minipay.entity.SettlementItem;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.SettlementMethod;
import com.mwkim.projecthub.minipay.exception.custom.CreatorNotFoundException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.SettlementRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public Settlement createSettlement(Long creatorId, BigDecimal totalAmount, List<Long> participantIds, SettlementMethod method) {
        User createdBy = userRepository.findById(creatorId)
                .orElseThrow(() -> new CreatorNotFoundException("Creator not found"));
        // 정산 참가자 조회
        List<User> participants = userRepository.findAllById(participantIds);

        Settlement settlement = Settlement.builder() // 내부에서 초기화 시 pending상태로 변환
                .createdBy(createdBy)
                .totalAmount(totalAmount)
                .build();

        List<BigDecimal> amounts = calculateSettlement(totalAmount, participants.size(), method);

        for (int i = 0; i < participants.size(); i++) {
            SettlementItem item = SettlementItem.builder()
                    .user(participants.get(i))
                    .amount(amounts.get(i))
                    .build();
            settlement.addItem(item);
        }

        return settlementRepository.save(settlement);
    }

    private List<BigDecimal> calculateSettlement(BigDecimal totalAmount, int numPeople, SettlementMethod method) {
        switch (method) {
            case EQUAL:
                return calculateEqualSettlement(totalAmount, numPeople);
            case RANDOM:
                return calculateRandomSettlement(totalAmount, numPeople);
            default:
                throw new IllegalArgumentException("Unsupported settlement method");
        }
    }

    // 균등 정산 (소수점 이하 금액을 마지막 참여자에게 할당)
    private List<BigDecimal> calculateEqualSettlement(BigDecimal totalAmount, int numPeople) {
        // amonut := 금액 / 참여자 수
        BigDecimal amount = totalAmount.divide(BigDecimal.valueOf(numPeople), 0, RoundingMode.DOWN);

        // 각 참여자 수 - 1에게 동일한 금액 할당 [333, 333]
        List<BigDecimal> amounts = new ArrayList<>(Collections.nCopies(numPeople - 1, amount));
        // ex) 1000 / 3 -> 333.3 -> 내림처리 : 333 -> 1번째 333, 2번째 333, 마지막 334원 청구


        // 마지막 사람에게 나머지 금액 할당   (total amount - amount * 2(인원수 )
        BigDecimal lastAmount = totalAmount.subtract(amount.multiply(BigDecimal.valueOf(numPeople - 1)));
        amounts.add(lastAmount);// [333, 333, 334]

        return amounts;
    }

    // 랜덤 정산 (총 금액을 참여자들에게 무작위로 분배하되, 모든 참여자가 최소 1원 이상을 부담하고 총 금액과 정확히 일치하도록 하는 방식)
    private List<BigDecimal> calculateRandomSettlement(BigDecimal totalAmount, int numPeople) {
        Random random = new Random();
        List<BigDecimal> amounts = new ArrayList<>(); // 정산 금액을 담을 리스트
        BigDecimal remaining = totalAmount; // 남은 금액

        for (int i = 0; i < numPeople - 1; i++) {
            // maxAmount := 1000 - (3(인원 수) - 1) = 998 (최대 금액 계산)
            BigDecimal maxAmount = remaining.subtract(BigDecimal.valueOf(numPeople - i - 1));

            // random.nextDouble()이 0.65라면, 0.65 * 998 = 649.7 -> 내림처리해서 649
            BigDecimal amount = BigDecimal.valueOf(random.nextDouble()).multiply(maxAmount)
                    .setScale(0, RoundingMode.DOWN);
            amounts.add(amount); // 첫 사용자는 649원 추가
            remaining = remaining.subtract(amount); // 남은 금액 갱신 -> 이렇게 사람수 -1만큼 돌아 금액을 정한다.
            // 금액이 얼마 낼지는 랜덤 값에 의해 결정됨
            // 이런 로직으로 인해 사용자가 최소 1원을 내는 것을 보장함.
        }
        amounts.add(remaining);

        return amounts;
    }


}
