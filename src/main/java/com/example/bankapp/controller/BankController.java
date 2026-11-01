package com.example.bankapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import com.example.bankapp.service.TransactionAuditService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;
    private final TransactionAuditService auditService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("customers", bankService.searchCustomers(keyword));
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("successCount", bankService.getSuccessCount());
        model.addAttribute("failedCount", bankService.getFailedCount());
        model.addAttribute("transactions", bankService.getLatestTransactions());
        model.addAttribute("transferRequest", new TransferRequest());
        
        model.addAttribute("customer", new Customer());
        
        model.addAttribute("keyword", keyword);
        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(@RequestParam String accountNumber,
                            @RequestParam String fullName,
                            @RequestParam java.math.BigDecimal balance,
                            @RequestParam String email,
                            RedirectAttributes redirectAttributes) {
        try {
            bankService.createCustomer(accountNumber, fullName, balance, email);
            redirectAttributes.addFlashAttribute("successMessage", "Customer added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String transfer(@Valid @ModelAttribute TransferRequest transferRequest,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation error in transfer form");
            return "redirect:/";
        }

        try {
            bankService.transfer(
                    transferRequest.getFromAccount(),
                    transferRequest.getToAccount(),
                    transferRequest.getAmount(),
                    transferRequest.isSimulateFailure()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully!");
        } catch (Exception e) {
            try {
                auditService.recordFailure(
                        transferRequest.getFromAccount(),
                        transferRequest.getToAccount(),
                        transferRequest.getAmount(),
                        e.getMessage()
                );
            } catch (Exception ex) {
            }
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer failed: " + e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String accountHistory(@PathVariable String accountNumber, Model model) {
        Customer customer = bankService.getCustomer(accountNumber);
        model.addAttribute("customer", customer);
        model.addAttribute("history", bankService.getAccountHistory(accountNumber));
        return "history";
    }
}