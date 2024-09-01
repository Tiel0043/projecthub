package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Settlement;
import com.mwkim.projecthub.minipay.entity.SettlementParticipant;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.SettlementType;
import com.mwkim.projecthub.minipay.exception.custom.ParticipantNotFoundException;
import com.mwkim.projecthub.minipay.exception.custom.SettlementNotFoundException;
import com.mwkim.projecthub.minipay.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SettlementServiceTest {

    private SettlementService settlementService;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        settlementService = new SettlementService(settlementRepository, userService);
    }

    @Test
    @DisplayName(" 1/n 정산 방식이 정확히 작동하는지 테스트")
    void testCreateSettlement_EqualShares() {
        // Given
        Long requesterId = 1L;
        BigDecimal totalAmount = BigDecimal.valueOf(1000);
        SettlementType type = SettlementType.EQUAL;
        List<Long> participantIds = Arrays.asList(2L, 3L, 4L);

        User requester = User.createUser("Requester");
        User participant1 = User.createUser("Participant1");
        User participant2 = User.createUser("Participant2");
        User participant3 = User.createUser("Participant3");

        when(userService.getUserById(1L)).thenReturn(requester);
        when(userService.getUserById(2L)).thenReturn(participant1);
        when(userService.getUserById(3L)).thenReturn(participant2);
        when(userService.getUserById(4L)).thenReturn(participant3);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Settlement settlement = settlementService.createSettlement(requesterId, totalAmount, type, participantIds);

        // Then
        List<BigDecimal> shares = settlement.getParticipants().stream()
                .map(SettlementParticipant::getAmount)
                .collect(Collectors.toList());

        assertThat(shares).hasSize(3);
        assertThat(shares.get(0)).isEqualByComparingTo("333");
        assertThat(shares.get(1)).isEqualByComparingTo("333");
        assertThat(shares.get(2)).isEqualByComparingTo("334");

        BigDecimal totalShare = shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalShare).isEqualByComparingTo(totalAmount);
    }

    @Test
    void testCreateSettlement_RandomShares() {
        // Given
        Long requesterId = 1L;
        BigDecimal totalAmount = BigDecimal.valueOf(1000);
        SettlementType type = SettlementType.RANDOM;
        List<Long> participantIds = Arrays.asList(2L, 3L, 4L);

        User requester = User.createUser("Requester");
        User participant1 = User.createUser("Participant1");
        User participant2 = User.createUser("Participant2");
        User participant3 = User.createUser("Participant3");

        when(userService.getUserById(1L)).thenReturn(requester);
        when(userService.getUserById(2L)).thenReturn(participant1);
        when(userService.getUserById(3L)).thenReturn(participant2);
        when(userService.getUserById(4L)).thenReturn(participant3);
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Settlement settlement = settlementService.createSettlement(requesterId, totalAmount, type, participantIds);

        // Then
        List<BigDecimal> shares = settlement.getParticipants().stream()
                .map(SettlementParticipant::getAmount)
                .collect(Collectors.toList());

        assertThat(shares).hasSize(3);

        BigDecimal totalShare = shares.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(totalShare).isEqualByComparingTo(totalAmount);

        assertThat(shares).allMatch(share -> share.compareTo(BigDecimal.ONE) >= 0);
    }

//    @Test
//    void testApproveSettlement_Success() {
//        // Given
//        Long participantId = 2L;
//        Long settlementId = 1L;
//
//        User user = User.createUser("Participant");
//
//        SettlementParticipant participant = SettlementParticipant.builder()
//                .user(user)
//                .build();
//
//        Settlement settlement = Settlement.builder()
//                .participants(Arrays.asList(participant))
//                .build();
//
//        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
//
//        // When
//        settlementService.approveSettlement(participantId, settlementId);
//
//        // Then
//        assertThat(participant.isApproved()).isTrue();
//        verify(settlementRepository).save(settlement);
//    }

    @Test
    void testApproveSettlement_ParticipantNotFound() {
        // Given
        Long participantId = 2L;
        Long settlementId = 1L;

        Settlement settlement = Settlement.builder().participants(new ArrayList<>()).build();

        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

        // When / Then
        assertThatThrownBy(() -> settlementService.approveSettlement(participantId, settlementId))
                .isInstanceOf(ParticipantNotFoundException.class);
    }

//    @Test
//    void testRejectSettlement_Success() {
//        // Given
//        Long participantId = 2L;
//        Long settlementId = 1L;
//
//        User user = User.createUser("Participant");
//
//        SettlementParticipant participant = SettlementParticipant.builder()
//                .user(user)
//                .build();
//
//        Settlement settlement = Settlement.builder()
//                .participants(Arrays.asList(participant))
//                .build();
//
//        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
//
//        // When
//        settlementService.rejectSettlement(participantId, settlementId);
//
//        // Then
//        assertThat(participant.isRejected()).isTrue();
//        verify(settlementRepository).save(settlement);
//    }
//
//    @Test
//    void testRejectSettlement_ParticipantNotFound() {
//        // Given
//        Long participantId = 2L;
//        Long settlementId = 1L;
//
//        Settlement settlement = Settlement.builder().participants(new ArrayList<>()).build();
//
//        when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));
//
//        // When / Then
//        assertThatThrownBy(() -> settlementService.rejectSettlement(participantId, settlementId))
//                .isInstanceOf(ParticipantNotFoundException.class);
//    }
//
//    @Test
//    void testCreateSettlement_InvalidType() {
//        // Given
//        Long requesterId = 1L;
//        BigDecimal totalAmount = BigDecimal.valueOf(1000);
//        SettlementType type = null; // Invalid type
//        List<Long> participantIds = Arrays.asList(2L, 3L, 4L);
//
//        User requester = User.createUser("Requester");
//
//        when(userService.getUserById(1L)).thenReturn(requester);
//
//        // When / Then
//        assertThatThrownBy(() -> settlementService.createSettlement(requesterId, totalAmount, type, participantIds))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("Unsupported settlement type");
//    }
}
