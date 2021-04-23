package com.vaadin.flow.spring.fusionsecurity;

import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    @Autowired
    private UserInfoRepository userInfoRepository;

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) context.getAuthentication().getPrincipal();
            return userDetails;
        }
        // Anonymous or no authentication.
        return null;
    }

    public UserInfo getAuthenticatedUserInfo() {
        UserDetails details = getAuthenticatedUser();
        if (details == null) {
            return null;
        }
        return userInfoRepository.findByUsername(details.getUsername());
    }

    public void logout() {
        new SecurityContextLogoutHandler().logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null,
                null);
    }

}
