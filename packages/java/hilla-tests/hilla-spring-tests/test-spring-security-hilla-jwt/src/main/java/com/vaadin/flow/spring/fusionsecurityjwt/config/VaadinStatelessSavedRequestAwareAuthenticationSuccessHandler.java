package com.vaadin.flow.spring.fusionsecurityjwt.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.flow.spring.fusionsecurityjwt.auth.JwtSplitCookieUtils;
import com.vaadin.flow.spring.security.VaadinSavedRequestAwareAuthenticationSuccessHandler;

import org.springframework.security.core.Authentication;

public class VaadinStatelessSavedRequestAwareAuthenticationSuccessHandler extends VaadinSavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        JwtSplitCookieUtils.setJwtSplitCookiesIfNecessary(request, response, authentication);
        super.onAuthenticationSuccess(request, response, authentication);

    }
}
