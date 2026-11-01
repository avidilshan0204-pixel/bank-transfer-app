package com.example.bankapp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional 
@RequiredArgsConstructor
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrdered();
    }

    @Transactional(readOnly = true)
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllCustomers();
        }
        return customerRepository.searchByName(keyword);
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(String accountNumber) {
        return customerRepository.getByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalBalance() {
        BigDecimal sum = customerRepository.getTotalBalance();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public long getSuccessCount() {
        return transactionRepository.countByStatus(TransactionStatus.SUCCESS);
    }

    @Transactional(readOnly = true)
    public long getFailedCount() {
        return transactionRepository.countByStatus(TransactionStatus.FAILED);
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    @Transactional(readOnly = true)
    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    @Transactional(rollbackFor = Exception.class)
    public void createCustomer(String accountNumber, String fullName, BigDecimal balance, String email) {
        if (customerRepository.countAccounts(accountNumber) > 0) {
            throw new BankException("Account number already exists: " + accountNumber);
        }
        Customer customer = new Customer(accountNumber, fullName, balance, email);
        customerRepository.save(customer);
    }

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
       
        if (fromAccount.equals(toAccount)) {
            throw new BankException("Sender and receiver accounts must be different");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Transfer amount must be greater than zero");
        }

        int updatedRows = customerRepository.debit(fromAccount, amount);
        if (updatedRows == 0) {
            throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount);
        }

        if (simulateFailure) {
            throw new RuntimeException("Simulated system failure after the withdrawal");
        }

        customerRepository.credit(toAccount, amount);

        BankTransaction tx = new BankTransaction();
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setDescription("Transfer completed");
        tx.setFromAccount(fromAccount);
        tx.setToAccount(toAccount);
        tx.setStatus(TransactionStatus.SUCCESS);

        return transactionRepository.save(tx);
    }
}