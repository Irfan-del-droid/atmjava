package com.example.atm.controller;

import com.example.atm.model.Account;
import com.example.atm.service.AtmService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/atm")
public class AtmController {

    @Autowired
    private AtmService atmService;

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId, @RequestParam String pin, HttpSession session, Model model) {
        Optional<Account> account = atmService.login(userId, pin);
        if (account.isPresent()) {
            session.setAttribute("account", account.get());
            return "redirect:/atm/dashboard";
        } else {
            model.addAttribute("error", "Invalid User ID or PIN");
            return "login";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/atm/login";
        }
        model.addAttribute("account", atmService.findAccountById(account.getId()).get());
        return "dashboard";
    }

    @GetMapping("/history")
    public String showTransactionHistory(HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/atm/login";
        }
        model.addAttribute("transactions", atmService.getTransactionHistory(account.getId()));
        return "history";
    }

    @GetMapping("/withdraw")
    public String showWithdrawPage() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/atm/login";
        }
        try {
            atmService.withdraw(account.getId(), amount);
            return "redirect:/atm/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "withdraw";
        }
    }

    @GetMapping("/deposit")
    public String showDepositPage() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount, HttpSession session) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/atm/login";
        }
        atmService.deposit(account.getId(), amount);
        return "redirect:/atm/dashboard";
    }

    @GetMapping("/transfer")
    public String showTransferPage() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam String toUserId, @RequestParam BigDecimal amount, HttpSession session, Model model) {
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            return "redirect:/atm/login";
        }
        try {
            atmService.transfer(account.getId(), toUserId, amount);
            return "redirect:/atm/dashboard";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "transfer";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/atm/login";
    }
}
