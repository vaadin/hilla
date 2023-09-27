package com.vaadin.flow.spring.fusionsecurity;

import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinWebSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    protected RequestUtil requestUtil;

    @Value("${springSecurityTestApp.security.stateless:false}")
    private boolean stateless;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Public access
        http.authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/public/**"))
                .permitAll();
        http.authorizeHttpRequests()
                .requestMatchers(
                        new AntPathRequestMatcher(applyUrlMapping("/")))
                .permitAll();
        http.authorizeHttpRequests()
                .requestMatchers(
                        new AntPathRequestMatcher(applyUrlMapping("/form")))
                .permitAll();
        http.authorizeHttpRequests().requestMatchers(
                new AntPathRequestMatcher(applyUrlMapping("/proxied-service")))
                .permitAll();

        // Admin only access
        http.authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/admin-only/**"))
                .hasAnyRole(ROLE_ADMIN);
        http.authorizeHttpRequests()
                .requestMatchers(new AntPathRequestMatcher("/error/**"))
                .permitAll();

        super.configure(http);
        setLoginView(http, "/login");
        http.logout().logoutUrl(applyUrlMapping("/logout"));

        if (stateless) {
            setStatelessAuthentication(http,
                    new SecretKeySpec(Base64.getUrlDecoder().decode(
                            "I72kIcB1UrUQVHVUAzgweE-BLc0bF8mLv9SmrgKsQAk"),
                            JwsAlgorithms.HS256),
                    "statelessapp");
        }
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager() {
            @Override
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException {
                UserInfo userInfo = userInfoRepository.findByUsername(username);
                if (userInfo == null) {
                    throw new UsernameNotFoundException(
                            "No user present with username: " + username);
                } else {
                    return new User(userInfo.getUsername(),
                            userInfo.getEncodedPassword(),
                            userInfo.getRoles().stream()
                                    .map(role -> new SimpleGrantedAuthority(
                                            "ROLE_" + role))
                                    .collect(Collectors.toList()));
                }
            }
        };
    }
}
