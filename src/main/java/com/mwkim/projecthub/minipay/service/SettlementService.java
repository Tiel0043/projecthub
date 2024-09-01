package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Settlement;
import com.mwkim.projecthub.minipay.entity.SettlementParticipant;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.SettlementType;
import com.mwkim.projecthub.minipay.exception.custom.ParticipantNotFoundException;
import com.mwkim.projecthub.minipay.exception.custom.SettlementNotFoundException;
import com.mwkim.projecthub.minipay.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final UserService userService;


    // 정산 생성
    public Settlement createSettlement(Long requesterId, BigDecimal totalAmount, SettlementType type, List<Long> participantIds) {
        User requester = userService.getUserById(requesterId);
        Settlement settlement = Settlement.createSettlement(requester, totalAmount, type);

        // 참여하는 모든 참여자들의 아이디 조회
        List<User> participants = participantIds.stream()
                .map(userService::getUserById)
                .collect(Collectors.toList());

        // shares : 각 정산 금액이 리스트마다 들어감.
        // calculateShares := 정산 금액 계산
        List<BigDecimal> shares = calculateShares(totalAmount, participants.size(), type);

        for (int i = 0; i < participants.size(); i++) {
            SettlementParticipant participant = SettlementParticipant.createSettlementParticipant(participants.get(i), shares.get(i));
            settlement.addParticipant(participant);
        }

        return settlementRepository.save(settlement);
    }

    /**
     * 정산 금액을 계산하는 메소드
     * @param totalAmount 총 금액
     * @param participantCount 참가자 수
     * @param type 정산 유형 (EQUAL 또는 RANDOM)
     * @return 각 참가자의 정산 금액 리스트
     */
    private List<BigDecimal> calculateShares(BigDecimal totalAmount, int participantCount, SettlementType type) {
        List<BigDecimal> shares = new ArrayList<>();

        switch (type) {
            case EQUAL:
                shares = calculateEqualShares(totalAmount, participantCount);
                break;
            case RANDOM:
                shares = calculateRandomShares(totalAmount, participantCount);
                break;
            default:
                throw new IllegalArgumentException("Unsupported settlement type: " + type);
        }

        return shares;
    }

    private List<BigDecimal> calculateEqualShares(BigDecimal totalAmount, int participantCount) {
        List<BigDecimal> shares = new ArrayList<>();
        // 총 정산 금액 / 인원수 -> 소수점은 버림처리 -> 1000 / 3 = 333.333  ---> 333
        BigDecimal share = totalAmount.divide(BigDecimal.valueOf(participantCount), 0, RoundingMode.DOWN);

        // 마지막 참가자를 제외한 모든 참가자에게 기본 금액 할당
        for (int i = 0; i < participantCount - 1; i++) { // -1을 하는 이유는 정산금액 보장을 위함(아래 더 설명)
            shares.add(share);
        }

        // 마지막 참가자에게 남은 금액 전체 할당
        // 1000 - 666 = 334 (이렇게 하면, 정산 금액을 보장할 수 있다.)
        BigDecimal lastShare = totalAmount.subtract(share.multiply(BigDecimal.valueOf(participantCount - 1)));
        shares.add(lastShare);

        return shares;
    }

    private List<BigDecimal> calculateRandomShares(BigDecimal totalAmount, int participantCount) {
        List<BigDecimal> shares = new ArrayList<>();
        Random random = new Random();

        BigDecimal totalDistributed = BigDecimal.ZERO;

        for (int i = 0; i < participantCount - 1; i++) {
            double randomFactor = random.nextDouble();
            BigDecimal share = totalAmount.multiply(BigDecimal.valueOf(randomFactor))
                    .setScale(0, RoundingMode.DOWN);

            // 최소 1원 보장
            if (share.compareTo(BigDecimal.ONE) < 0) {
                share = BigDecimal.ONE;
            }

            shares.add(share);
            totalDistributed = totalDistributed.add(share);
        }

        // 마지막 참가자에게 남은 금액 할당
        BigDecimal lastShare = totalAmount.subtract(totalDistributed);
        shares.add(lastShare);

        return shares;
    }

    // 정산 승인
    public void approveSettlement(Long participantId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found"));

        SettlementParticipant participant = settlement.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotFoundException("Participant not found"));

        participant.approve();
        settlementRepository.save(settlement);
    }

    // 정산 거절
    public void rejectSettlement(Long participantId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException("Settlement not found"));

        SettlementParticipant participant = settlement.getParticipants().stream()
                .filter(p -> p.getUser().getId().equals(participantId))
                .findFirst()
                .orElseThrow(() -> new ParticipantNotFoundException("Participant not found"));

        participant.reject();
        settlementRepository.save(settlement);
    }


}
