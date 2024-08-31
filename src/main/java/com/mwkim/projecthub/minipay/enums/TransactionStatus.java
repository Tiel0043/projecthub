package com.mwkim.projecthub.minipay.enums;

public enum TransactionStatus {
    PENDING,    // 송금이 요청되었지만 아직 수령되지 않은 상태
    COMPLETED,  // 송금이 완료된 상태
    CANCELLED,  // 송금이 취소된 상태
    EXPIRED     // Pending 상태에서 72시간이 지나 만료된 상태
}