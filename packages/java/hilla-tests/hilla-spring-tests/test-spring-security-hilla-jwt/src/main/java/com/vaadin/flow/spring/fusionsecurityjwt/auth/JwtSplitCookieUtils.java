package com.vaadin.flow.spring.fusionsecurityjwt.auth;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.core.ResolvableType;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.WebUtils;

public class JwtSplitCookieUtils {
    public static final String JWT_HEADER_AND_PAYLOAD_COOKIE_NAME = "jwt.headerAndPayload";
    public static final String JWT_SIGNATURE_COOKIE_NAME = "jwt.signature";

    public static String getTokenFromSplitCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        Cookie jwtHeaderAndPayload = WebUtils
                .getCookie(request, JWT_HEADER_AND_PAYLOAD_COOKIE_NAME);
        if (jwtHeaderAndPayload == null) {
            return null;
        }

        Cookie jwtSignature = WebUtils
                .getCookie(request, JWT_SIGNATURE_COOKIE_NAME);
        if (jwtSignature == null) {
            return null;
        }

        return jwtHeaderAndPayload.getValue() + "." + jwtSignature.getValue();
    }

    public static void setJwtSplitCookiesIfNecessary(HttpServletRequest request,
            HttpServletResponse response, Authentication authentication) {
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getWebApplicationContext(servletContext);

        JWKSource<SecurityContext> jwkSource = (JWKSource<SecurityContext>) webApplicationContext
                .getBean(webApplicationContext.getBeanNamesForType(
                        ResolvableType.forClassWithGenerics(JWKSource.class,
                                SecurityContext.class))[0]);

        final long EXPIRES_IN = 3600L;

        final Date now = new Date();

        final String rolePrefix = "ROLE_";
        final String scope = authentication.getAuthorities().stream()
                .map(Objects::toString).filter(a -> a.startsWith(rolePrefix))
                .map(a -> a.substring(rolePrefix.length()))
                .collect(Collectors.joining(" "));

        SignedJWT signedJWT;
        try {
            JWSAlgorithm jwsAlgorithm = JWSAlgorithm.HS256;
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
            JWKSelector jwkSelector = new JWKSelector(
                    JWKMatcher.forJWSHeader(jwsHeader));

            List<JWK> jwks = jwkSource.get(jwkSelector, null);
            JWK jwk = jwks.get(0);

            JWSSigner signer = new DefaultJWSSignerFactory()
                    .createJWSSigner(jwk, jwsAlgorithm);
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(authentication.getName()).issuer("statelessapp")
                    .issueTime(now)
                    .expirationTime(new Date(now.getTime() + EXPIRES_IN * 1000))
                    .claim("scope", scope).build();
            signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),
                    claimsSet);
            signedJWT.sign(signer);

            Cookie headerAndPayload = new Cookie(
                    JWT_HEADER_AND_PAYLOAD_COOKIE_NAME,
                    new String(signedJWT.getSigningInput(),
                            StandardCharsets.UTF_8));
            headerAndPayload.setSecure(true);
            headerAndPayload.setHttpOnly(false);
            headerAndPayload.setPath(request.getContextPath() + "/");
            headerAndPayload.setMaxAge((int) EXPIRES_IN - 1);
            response.addCookie(headerAndPayload);

            Cookie signature = new Cookie(JWT_SIGNATURE_COOKIE_NAME,
                    signedJWT.getSignature().toString());
            signature.setHttpOnly(true);
            signature.setSecure(true);
            signature.setPath(request.getContextPath() + "/");
            signature.setMaxAge((int) EXPIRES_IN - 1);
            response.addCookie(signature);
        } catch (JOSEException e) {
            e.printStackTrace();
        }

    }

    public static void removeJwtSplitCookies(HttpServletRequest request,
            HttpServletResponse response) {
        Cookie jwtHeaderAndPayloadRemove = new Cookie(
                JWT_HEADER_AND_PAYLOAD_COOKIE_NAME, null);
        jwtHeaderAndPayloadRemove.setPath(request.getContextPath() + "/");
        jwtHeaderAndPayloadRemove.setMaxAge(0);
        jwtHeaderAndPayloadRemove.setSecure(request.isSecure());
        jwtHeaderAndPayloadRemove.setHttpOnly(false);
        response.addCookie(jwtHeaderAndPayloadRemove);

        Cookie jwtSignatureRemove = new Cookie(JWT_SIGNATURE_COOKIE_NAME, null);
        jwtSignatureRemove.setPath(request.getContextPath() + "/");
        jwtSignatureRemove.setMaxAge(0);
        jwtSignatureRemove.setSecure(request.isSecure());
        jwtSignatureRemove.setHttpOnly(true);
        response.addCookie(jwtSignatureRemove);
    }
}
