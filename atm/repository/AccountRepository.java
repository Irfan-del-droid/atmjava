package com.example.atm.repository;

import com.example.atm.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserIdAndPin(String userId, String pin);

    Optional<Account> findByUserId(String userId);
}
