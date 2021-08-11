package com.vaadin.flow.spring.fusionsecurityjwt.auth;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;

public class JwtSplitCookieBearerTokenConverterFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        final String tokenFromSplitCookies = JwtSplitCookieUtils
                .getTokenFromSplitCookies((HttpServletRequest) request);
        if (tokenFromSplitCookies != null) {
            HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(
                    (HttpServletRequest) request) {
                @Override
                public String getHeader(String headerName) {
                    if ("Authorization".equals(headerName)) {
                        return "Bearer " + tokenFromSplitCookies;
                    }
                    return super.getHeader(headerName);
                }
            };
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

}
