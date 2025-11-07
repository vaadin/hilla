/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
        return accountRepository.findByOwner(name).map(Account::getBalance)
                .orElse(null);
    }
}
