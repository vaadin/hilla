package com.vaadin.flow.spring.fusionsecurity.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.vaadin.flow.spring.fusionsecurity.SecurityUtils;
import com.vaadin.flow.spring.fusionsecurity.data.Account;
import com.vaadin.flow.spring.fusionsecurity.data.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BankService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SecurityUtils utils;

    public void applyForLoan() {
        String name = utils.getAuthenticatedUser().getUsername();
        Optional<Account> acc = accountRepository.findByOwner(name);
        if (!acc.isPresent()) {
            return;
        }
        Account account = acc.get();
        account.setBalance(account.getBalance().add(new BigDecimal("10000")));
        accountRepository.save(account);
    }

    public BigDecimal getBalance() {
        String name = utils.getAuthenticatedUser().getUsername();
        return accountRepository.findByOwner(name).map(Account::getBalance).orElse(null);
    }
}
