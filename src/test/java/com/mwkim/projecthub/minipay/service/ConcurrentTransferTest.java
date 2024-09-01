package com.mwkim.projecthub.minipay.service;

import com.mwkim.projecthub.minipay.entity.Account;
import com.mwkim.projecthub.minipay.entity.User;
import com.mwkim.projecthub.minipay.enums.AccountType;
import com.mwkim.projecthub.minipay.exception.custom.CollisionException;
import com.mwkim.projecthub.minipay.exception.custom.DailyLimitExceedException;
import com.mwkim.projecthub.minipay.exception.custom.InsufficientBalanceException;
import com.mwkim.projecthub.minipay.repository.AccountRepository;
import com.mwkim.projecthub.minipay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ConcurrentTransferTest {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentTransferTest.class);


    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    private User testUser;
    private Account mainAccount;
    private Account savingsAccount;

    @BeforeEach
    void setUp() {
        testUser = userService.registerUser(12L, "TestUser");
        mainAccount = accountService.createAccount(testUser.getId(), AccountType.MAIN, new BigDecimal("3000000"));
        savingsAccount = accountService.createAccount(testUser.getId(), AccountType.SAVINGS, BigDecimal.ZERO);
    }

    @Test
    void testCreateAccount() {
        System.out.println("zzzzzzz");
        assertNotNull(mainAccount);
        assertEquals(AccountType.MAIN, mainAccount.getType());
        assertEquals(BigDecimal.ZERO, mainAccount.getBalance());
        assertEquals(new BigDecimal("3000000"), mainAccount.getDailyLimitAmount());

        assertNotNull(savingsAccount);
        assertEquals(AccountType.SAVINGS, savingsAccount.getType());
        assertEquals(BigDecimal.ZERO, savingsAccount.getBalance());
    }

    @Test
    void testDeposit() {
        accountService.deposit(mainAccount, new BigDecimal("1000"));
        assertEquals(new BigDecimal("1000"), mainAccount.getBalance());
    }

    @Test
    void testWithdraw() {
        accountService.deposit(mainAccount, new BigDecimal("1000"));
        accountService.withdraw(mainAccount, new BigDecimal("500"));
        assertEquals(new BigDecimal("500"), mainAccount.getBalance());
    }

    @Test
    void testWithdrawInsufficientBalance() {
        assertThrows(InsufficientBalanceException.class, () -> {
            accountService.withdraw(mainAccount, new BigDecimal("1000"));
        });
    }

    @Test
    void testTransfer() {
        User friendUser = userService.registerUser(13L, "FriendUser");
        Account friendAccount = accountService.createAccount(friendUser.getId(), AccountType.MAIN, new BigDecimal("3000000"));

        accountService.deposit(mainAccount, new BigDecimal("1000"));
        accountService.transfer(mainAccount.getId(), friendAccount.getId(), new BigDecimal("500"));

        assertEquals(new BigDecimal("500"), mainAccount.getBalance());
        assertEquals(new BigDecimal("500"), friendAccount.getBalance());
    }

    @Test
    void testTransferWithAutoCharge() {
        User friendUser = userService.registerUser(13L, "FriendUser");
        Account friendAccount = accountService.createAccount(friendUser.getId(), AccountType.MAIN, new BigDecimal("3000000"));

        accountService.transfer(mainAccount.getId(), friendAccount.getId(), new BigDecimal("1000"));

        assertEquals(new BigDecimal("9000"), mainAccount.getBalance());
        assertEquals(new BigDecimal("1000"), friendAccount.getBalance());
    }

    @Test
    void testDailyLimitExceeded() {
        assertThrows(DailyLimitExceedException.class, () -> {
            accountService.transfer(mainAccount.getId(), savingsAccount.getId(), new BigDecimal("3000001"));
        });
    }

    @Test
    void testConcurrentTransfers() throws InterruptedException {

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        User receiverUser = userService.registerUser(13L, "ReceiverUser");
        Account receiverAccount = accountService.createAccount(receiverUser.getId(), AccountType.MAIN, new BigDecimal("3000000"));

        log.debug("Initial main account balance: {}", mainAccount.getBalance());
        log.debug("Initial receiver account balance: {}", receiverAccount.getBalance());

        accountService.deposit(mainAccount, new BigDecimal("1000000")); // 금액 충전
        log.debug("deposit main account balance: {}", mainAccount.getBalance());


        // 여기서 final 변수로 ID를 저장합니다.
        final Long mainAccountId = mainAccount.getId();
        final Long receiverAccountId = receiverAccount.getId();

        AtomicInteger successCount = new AtomicInteger(0);
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    System.out.println("i = ");
//                    accountService.transfer(mainAccountId, receiverAccountId, new BigDecimal("100"));
                    successCount.incrementAndGet();
                } catch (CollisionException e) {
                    // 충돌 로그
                    log.info("충돌 발생!");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 스레드 끝나기 대기
        executorService.shutdown();
        log.debug("All transfers completed");
        log.info("successCount : " + successCount);

        // 테스트 결과를 확인하기 위해 계정 상태를 새로 조회합니다.
        Account updatedMainAccount = accountService.getAccountById(mainAccountId);
        Account updatedReceiverAccount = accountService.getAccountById(receiverAccountId);
        log.debug("updated main account balance: {}", updatedMainAccount.getBalance());
        log.debug("updated receiver account balance: {}", updatedMainAccount.getBalance());


        assertEquals(new BigDecimal("990000"), updatedMainAccount.getBalance());
        assertEquals(new BigDecimal("10000"), updatedReceiverAccount.getBalance());
    }
}