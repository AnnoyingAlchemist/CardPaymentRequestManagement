package com.capgemini.demo;

import com.capgemini.demo.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.Assert.*;

public class JwtServiceTest {
    @Test
    void verifySignatureAndExtractClaims_validToken_returnsClaims() {
        JwtService service = new JwtService();

        String jwt = Jwts.builder()
                .setSubject("peter@example.com")
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(Keys.hmacShaKeyFor(JwtService.SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        Claims claims = service.verifySignatureAndExtractClaims(jwt);

        assertEquals("peter@example.com", claims.getSubject());
    }

    @Test
    void extractUsername_returnsSubject() {
        JwtService service = new JwtService();

        String jwt = Jwts.builder()
                .setSubject("peter@example.com")
                .setExpiration(new Date(System.currentTimeMillis() + 100000))
                .signWith(Keys.hmacShaKeyFor(JwtService.SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertEquals("peter@example.com", service.extractUsername(jwt));
    }

    @Test
    void getExpiration_returnsExpirationDate() {
        JwtService service = new JwtService();

        // Make exp second-precision to match JWT round-trip
        Date exp = new Date(((System.currentTimeMillis() / 1000) + 50) * 1000);

        String jwt = Jwts.builder()
                .setSubject("anything")
                .setExpiration(exp)
                .signWith(Keys.hmacShaKeyFor(JwtService.SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertEquals(exp, service.getExpiration(jwt));
    }

    @Test
    void isTokenExpired_falseForFutureExpiration() {
        JwtService service = new JwtService();

        String jwt = Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Keys.hmacShaKeyFor(JwtService.SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        Assertions.assertFalse(service.isTokenExpired(jwt));
    }

    @Test
    void isTokenExpired_trueForPastExpiration() {
        JwtService service = new JwtService();

        String jwt = Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(System.currentTimeMillis() - 10000)) // already expired
                .signWith(Keys.hmacShaKeyFor(JwtService.SECRET.getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertTrue(service.isTokenExpired(jwt));
    }

    @Test
    void invalidSignature_isTreatedAsExpiredOrInvalid() {
        JwtService service = new JwtService();

        String jwt = Jwts.builder()
                .setSubject("test")
                .setExpiration(new Date(((System.currentTimeMillis()/1000)+60)*1000))
                .signWith(Keys.hmacShaKeyFor("different-secret-different-secret-123456".getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertTrue(service.isTokenExpired(jwt)); // per our policy in catch(JwtException)
    }

    @Test
    void verifySignature_invalidSignature_throwsException() {
        JwtService service = new JwtService();

        // create token with WRONG key
        String jwt = Jwts.builder()
                .setSubject("peter")
                .signWith(Keys.hmacShaKeyFor("wrongsecretwrongsecretwrongsecret11".getBytes()), SignatureAlgorithm.HS256)
                .compact();

        assertThrows(Exception.class, () -> service.verifySignatureAndExtractClaims(jwt));
    }
}
