package com.example.bankapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionAuditService {

    private final BankTransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String fromAccount, String toAccount, BigDecimal amount, String errorMessage) {
        BankTransaction tx = new BankTransaction();
        tx.setAmount(amount != null ? amount : BigDecimal.ZERO);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setDescription(errorMessage);
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setStatus(TransactionStatus.FAILED);

        transactionRepository.save(tx);
    }
}