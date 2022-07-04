package com.vaadin.flow.spring.fusionsecurity.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

    public UserInfo findByUsername(String username);
}
