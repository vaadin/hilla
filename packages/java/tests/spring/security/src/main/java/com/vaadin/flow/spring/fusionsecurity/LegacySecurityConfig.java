package com.vaadin.flow.spring.fusionsecurity;

import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Profile("legacy-vaadin-web-security")
public class LegacySecurityConfig extends VaadinWebSecurity {

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http.authorizeHttpRequests(auth -> auth.requestMatchers("/public/**")
                .permitAll().requestMatchers(applyUrlMapping("/")).permitAll()
                .requestMatchers(applyUrlMapping("/form")).permitAll()
                .requestMatchers("/admin-only/**").hasRole("ADMIN")
                .requestMatchers("/error/**").permitAll().anyRequest()
                .authenticated());

        super.configure(http);
        setLoginView(http, "/login", applyUrlMapping("/"));
        http.logout(cfg -> cfg.logoutUrl(applyUrlMapping("/logout")));

        if (stateless) {
            setStatelessAuthentication(http,
                    new SecretKeySpec(Base64.getUrlDecoder().decode(
                            "I72kIcB1UrUQVHVUAzgweE-BLc0bF8mLv9SmrgKsQAk"),
                            JwsAlgorithms.HS256),
                    "statelessapp");
        }
        return http.build();
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
