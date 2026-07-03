package com.project.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtService {
    @Value(value = "${jwt.expiration}")
    private Integer expiration;
    @Value(value = "${jwt.secret}")
    private String secret;
    private final String AUTH_Header = "Authorization";

    public String generateJWT(String username){
        log.debug("IN JwtService: generateJWT");
        String jwt = Jwts.builder()
                .subject(username)
                .expiration(new Date(System.currentTimeMillis()+ TimeUnit.MINUTES.toMillis(expiration)))
                .signWith(getSignKey())
                .compact();
        log.debug("OUT JwtService: generateJWT");
        return jwt;
    }

    private Key getSignKey(){
        log.debug("IN JwtService: getSignKey");
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        Key resultKey = Keys.hmacShaKeyFor(keyBytes);
        log.debug("OUT JwtService: getSignKey");
        return resultKey;
    }

    public Optional<String> getTokenFromServletRequest(ServletRequest servletRequest){
        log.debug("IN JwtService: getTokenFromServletRequest");
        try {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String bearerToken = request.getHeader(AUTH_Header);
            if(bearerToken != null && bearerToken.startsWith("Bearer ")){
                return Optional.of(bearerToken.substring(7));
            }
            return Optional.empty();
        }finally {
            log.debug("OUT JwtService: getTokenFromServletRequest");
        }
    }

    public Optional<String> getUsernameFromJwt(String jwt){
        log.debug("IN JwtService:getUsernameFromJwt");
        try {
            return Optional.of(Jwts.parser()
                    .verifyWith((SecretKey) getSignKey())
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject());
        }catch (Exception e) {
            log.info("Can't take login from jwt: {}", e.getMessage());
        }finally {
            log.debug("OUT JwtService:getUsernameFromJwt");
        }
        return Optional.empty();
    }
}
