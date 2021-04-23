package com.vaadin.flow.spring.fusionsecurity;

import java.math.BigDecimal;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.fusionsecurity.data.Account;
import com.vaadin.flow.spring.fusionsecurity.data.AccountRepository;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(UserInfoRepository userInfoRepository, AccountRepository accountRepository,
            PasswordEncoder encoder) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userInfoRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            logger.info("Generating demo data");

            logger.info("... generating 2 UserInfo entities...");
            UserInfo user = new UserInfo("john", encoder.encode("john"), "John the User", "public/profiles/user.svg",
                    SecurityConfig.ROLE_USER);
            UserInfo admin = new UserInfo("emma", encoder.encode("emma"), "Emma the Admin", "public/profiles/admin.svg",
                    SecurityConfig.ROLE_ADMIN);
            user = userInfoRepository.save(user);
            admin = userInfoRepository.save(admin);

            logger.info("... generating 2 Account entities...");
            Account userAccount = new Account();
            userAccount.setOwner(user);
            userAccount.setBalance(new BigDecimal("10000"));

            Account adminAccount = new Account();
            adminAccount.setOwner(admin);
            adminAccount.setBalance(new BigDecimal("200000"));
            accountRepository.save(userAccount);
            accountRepository.save(adminAccount);

            logger.info("Generated demo data");
        };
    }

}