package com.vaadin.flow.spring.fusionsecurityjwt;

import java.util.Collections;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Primary
public class JwtSecurityUtils
        extends com.vaadin.flow.spring.fusionsecurity.SecurityUtils {

    public UserDetails getAuthenticatedUser() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            String userName = ((Jwt) principal).getClaim("sub");
            return new User(userName, "", Collections.emptyList());
        }
        // Anonymous or no authentication.
        return null;
    }

}
