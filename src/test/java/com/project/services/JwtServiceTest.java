package com.project.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private final String SECRET = "mySuperSecretKeyForJwtThatIsAtLeast32BytesLong";
    private final Integer EXPIRATION_MINUTES = 15;
    private final String USERNAME = "john_doe";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION_MINUTES);
    }

    @Test
    void shouldGenerateValidJwt() {
        String jwt = jwtService.generateJWT(USERNAME);
        assertThat(jwt).isNotBlank();

        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(jwt)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(USERNAME);
        assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now()));
    }

    @Test
    void shouldExtractToken_whenAuthorizationHeaderContainsBearerToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSJ9.someSignature";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        Optional<String> result = jwtService.getTokenFromServletRequest(request);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(token);
    }

    @Test
    void shouldReturnEmpty_whenAuthorizationHeaderMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        Optional<String> result = jwtService.getTokenFromServletRequest(request);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenAuthorizationHeaderDoesNotStartWithBearer() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        Optional<String> result = jwtService.getTokenFromServletRequest(request);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowClassCastException_whenServletRequestIsNotHttpServletRequest() {
        ServletRequest request = mock(ServletRequest.class);

        assertThatThrownBy(() -> jwtService.getTokenFromServletRequest(request))
                .isInstanceOf(ClassCastException.class);
    }

    @Test
    void shouldExtractUsername_whenJwtIsValid() {
        String jwt = jwtService.generateJWT(USERNAME);

        Optional<String> result = jwtService.getUsernameFromJwt(jwt);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(USERNAME);
    }

    @Test
    void shouldReturnEmpty_whenJwtIsInvalid() {
        String invalidJwt = "invalid.token.signature";

        Optional<String> result = jwtService.getUsernameFromJwt(invalidJwt);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenJwtIsMalformed() {
        String malformedJwt = "malformed";

        Optional<String> result = jwtService.getUsernameFromJwt(malformedJwt);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenJwtHasWrongSignature() {
        String wrongSecret = "anotherSecretKeyThatIsAlsoLongEnough";
        String jwt = Jwts.builder()
                .subject(USERNAME)
                .expiration(Date.from(Instant.now().plus(Duration.ofMinutes(EXPIRATION_MINUTES))))
                .signWith(Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<String> result = jwtService.getUsernameFromJwt(jwt);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmpty_whenJwtIsExpired() {
        String expiredJwt = Jwts.builder()
                .subject(USERNAME)
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<String> result = jwtService.getUsernameFromJwt(expiredJwt);

        assertThat(result).isEmpty();
    }
}