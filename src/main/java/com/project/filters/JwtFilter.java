package com.project.filters;

import com.project.services.CustomUserDetailService;
import com.project.services.JwtService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;


@Slf4j
@RequiredArgsConstructor
@Component
public class JwtFilter implements Filter {
    private final JwtService jwtService;
    private final CustomUserDetailService customUserDetailService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException{
        log.debug("IN JwtFilter: doFilter");
        Optional<String> jwt = jwtService.getTokenFromServletRequest(servletRequest);
        if(jwt.isPresent()){
            Optional<String> username = jwtService.getUsernameFromJwt(jwt.get());
            if(username.isPresent()){
                UserDetails userDetail = customUserDetailService.loadUserByUsername(username.get());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("Authentication successful for {}", username);
            }
        }
        log.debug("OUT JwtFilter; doFilter");
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
