package com.vaadin.flow.spring.fusionsecurity.endpoints;

import javax.annotation.security.PermitAll;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.spring.fusionsecurity.service.BankService;
import com.vaadin.fusion.Endpoint;

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
}
