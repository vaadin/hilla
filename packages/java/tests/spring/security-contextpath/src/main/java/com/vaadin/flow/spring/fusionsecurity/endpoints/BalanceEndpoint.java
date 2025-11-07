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
package com.vaadin.flow.spring.fusionsecurity.endpoints;

import jakarta.annotation.security.PermitAll;
import java.math.BigDecimal;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.spring.fusionsecurity.service.BankService;
import com.vaadin.hilla.Endpoint;
import reactor.core.publisher.Flux;

@Endpoint
@PermitAll
public class BalanceEndpoint {

    @Autowired
    private BankService bankService;

    public BigDecimal getBalance() {
        return bankService.getBalance();
    }

    public void applyForLoan() {
        bankService.applyForLoan();
    }

    public Flux<BigDecimal> getBalanceUpdates() {
        return Flux.fromStream(Stream.of(bankService.getBalance()));
    }
}
