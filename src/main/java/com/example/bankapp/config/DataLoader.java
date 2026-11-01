package com.example.bankapp.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) throws Exception {
        if (customerRepository.count() == 0) {
            customerRepository.save(new Customer("100001", "Kamal", new BigDecimal("50000.00"), "kamal@gmail.com"));
            customerRepository.save(new Customer("100002", "Nimali", new BigDecimal("25000.00"), "nimali@gmail.com"));
            customerRepository.save(new Customer("100003", "Saman", new BigDecimal("10000.00"), "saman@gmail.com"));
        }
    }
}