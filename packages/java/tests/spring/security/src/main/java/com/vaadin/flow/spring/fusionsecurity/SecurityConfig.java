package com.vaadin.flow.spring.fusionsecurity;

import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.spec.SecretKeySpec;

import com.vaadin.flow.spring.fusionsecurity.data.UserInfo;
import com.vaadin.flow.spring.fusionsecurity.data.UserInfoRepository;
import com.vaadin.flow.spring.security.RequestUtil;
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration;
import com.vaadin.flow.spring.security.stateless.VaadinStatelessSecurityConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

@EnableWebSecurity
@Configuration
@Import(VaadinAwareSecurityContextHolderStrategyConfiguration.class)
@Profile("default")
public class SecurityConfig {

    public static String ROLE_USER = "user";
    public static String ROLE_ADMIN = "admin";

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Value("${springSecurityTestApp.security.stateless:false}")
    private boolean stateless;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain vaadinSecurityFilterChain(HttpSecurity http,
            RequestUtil requestUtil) throws Exception {
        // Public access
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(new AntPathRequestMatcher("/public/**"))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(
                            requestUtil.applyUrlMapping("/")))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(
                            requestUtil.applyUrlMapping("/form")))
                    .permitAll()
                    .requestMatchers(new AntPathRequestMatcher(
                            requestUtil.applyUrlMapping("/proxied-service")))
                    .permitAll();

            // Admin only access
            auth.requestMatchers(new AntPathRequestMatcher("/admin-only/**"))
                    .hasAnyRole(ROLE_ADMIN)
                    .requestMatchers(new AntPathRequestMatcher("/error/**"))
                    .permitAll();
        });

        if (stateless) {
            // Disable creating and using sessions in Spring Security
            http.sessionManagement((sessionManagement) -> {
                sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            });
        }

        http.with(vaadin(), cfg -> cfg.loginView("/login",
                requestUtil.applyUrlMapping("/")));
        http.logout(
                cfg -> cfg.logoutUrl(requestUtil.applyUrlMapping("/logout")));

        if (stateless) {
            VaadinStatelessSecurityConfigurer.apply(http, cfg -> cfg
                    .withSecretKey(keyCfg -> keyCfg.secretKey(new SecretKeySpec(
                            Base64.getUrlDecoder().decode(
                                    "I72kIcB1UrUQVHVUAzgweE-BLc0bF8mLv9SmrgKsQAk"),
                            JwsAlgorithms.HS256)))
                    .issuer("statelessapp"));
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
