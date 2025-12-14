package com.example.atm.service;

import com.example.atm.model.Account;
import com.example.atm.model.Transaction;
import com.example.atm.model.TransactionType;
import com.example.atm.repository.AccountRepository;
import com.example.atm.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AtmService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Optional<Account> login(String userId, String pin) {
        return accountRepository.findByUserIdAndPin(userId, pin);
    }

    public Optional<Account> findAccountByUserId(String userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<Account> findAccountById(Long id) {
        return accountRepository.findById(id);
    }

    public List<Transaction> getTransactionHistory(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription("Deposit");
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());

        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getBalance());
        transaction.setDescription("Withdrawal");
        transaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        return accountRepository.save(account);
    }

    @Transactional
    public Account transfer(Long fromAccountId, String toUserId, BigDecimal amount) {
        Account fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByUserId(toUserId)
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        fromAccount.setUpdatedAt(LocalDateTime.now());

        toAccount.setBalance(toAccount.getBalance().add(amount));
        toAccount.setUpdatedAt(LocalDateTime.now());

        Transaction fromTransaction = new Transaction();
        fromTransaction.setAccount(fromAccount);
        fromTransaction.setTransactionType(TransactionType.TRANSFER_OUT);
        fromTransaction.setAmount(amount);
        fromTransaction.setRecipient(toAccount);
        fromTransaction.setBalanceAfter(fromAccount.getBalance());
        fromTransaction.setDescription("Transfer to " + toAccount.getAccountHolderName());
        fromTransaction.setCreatedAt(LocalDateTime.now());

        Transaction toTransaction = new Transaction();
        toTransaction.setAccount(toAccount);
        toTransaction.setTransactionType(TransactionType.TRANSFER_IN);
        toTransaction.setAmount(amount);
        toTransaction.setBalanceAfter(toAccount.getBalance());
        toTransaction.setDescription("Transfer from " + fromAccount.getAccountHolderName());
        toTransaction.setCreatedAt(LocalDateTime.now());

        transactionRepository.save(fromTransaction);
        transactionRepository.save(toTransaction);
        accountRepository.save(fromAccount);
        return accountRepository.save(toAccount);
    }
}
