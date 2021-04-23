package com.vaadin.flow.spring.fusionsecurity.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, String> {

    @Query("Select a from Account a where a.owner.username = ?1")
    Optional<Account> findByOwner(String username);

}
