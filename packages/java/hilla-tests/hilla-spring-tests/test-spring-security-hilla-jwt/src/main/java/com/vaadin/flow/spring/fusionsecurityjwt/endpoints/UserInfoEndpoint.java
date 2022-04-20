package com.vaadin.flow.spring.fusionsecurityjwt.endpoints;

import dev.hilla.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.fusionsecurity.SecurityUtils;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;

@Endpoint
@AnonymousAllowed
public class UserInfoEndpoint {

    @Autowired
    private SecurityUtils utils;

    public UserInfo getUserInfo() {
        return utils.getAuthenticatedUserInfo();
    }
}
