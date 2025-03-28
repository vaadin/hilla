package com.vaadin.flow.spring.fusionsecurity.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {

    public UserInfo findByUsername(String username);
}
