package com.vaadin.flow.spring.fusionsecurityjwt.endpoints;

import com.vaadin.fusion.Endpoint;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.fusionsecurity.SecurityUtils;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;

import org.springframework.beans.factory.annotation.Autowired;

@Endpoint
@AnonymousAllowed
public class UserInfoEndpoint {

    @Autowired
    private SecurityUtils utils;

    public UserInfo getUserInfo() {
        return utils.getAuthenticatedUserInfo();
    }
}
