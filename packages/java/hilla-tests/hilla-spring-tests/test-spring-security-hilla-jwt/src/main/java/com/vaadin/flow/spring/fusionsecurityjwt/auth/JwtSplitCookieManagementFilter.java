package com.vaadin.flow.spring.fusionsecurityjwt.auth;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtSplitCookieManagementFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null ||
                authentication instanceof AnonymousAuthenticationToken) {
            // Token authentication failed — remove the cookies
            JwtSplitCookieUtils
                    .removeJwtSplitCookies((HttpServletRequest) request,
                            (HttpServletResponse) response);
        } else {
            JwtSplitCookieUtils
                    .setJwtSplitCookiesIfNecessary((HttpServletRequest) request,
                            (HttpServletResponse) response, authentication);
        }

        chain.doFilter(request, response);
    }

}
