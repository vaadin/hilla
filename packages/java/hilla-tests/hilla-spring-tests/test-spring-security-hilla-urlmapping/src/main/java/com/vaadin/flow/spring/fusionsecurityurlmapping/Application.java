package com.vaadin.flow.spring.fusionsecurityurlmapping;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

@SpringBootApplication
public class Application
        extends com.vaadin.flow.spring.fusionsecurity.Application {

    protected static final String URL_MAPPING = "/urlmapping";

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // Test views use relative path to images, that cannot be correctly resolved
    // when setting vaadin.urlMapping, because view base path differs from
    // web application context path.
    // The following filter forwards request from
    // {vaadin.urlMapping}/public/images to /public/images, so they are then
    // served by spring.
    @Bean
    FilterRegistrationBean<?> publicImagesAliasFilter() {
        FilterRegistrationBean<OncePerRequestFilter> registrationBean = new FilterRegistrationBean<>(
                new OncePerRequestFilter() {

                    @Override
                    protected void doFilterInternal(HttpServletRequest request,
                            HttpServletResponse response,
                            FilterChain filterChain)
                            throws ServletException, IOException {
                        request.getRequestDispatcher(request.getRequestURI()
                                .substring(URL_MAPPING.length()))
                                .forward(request, response);
                    }
                });
        registrationBean.addUrlPatterns(URL_MAPPING + "/public/images/*",
                URL_MAPPING + "/public/profiles/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
