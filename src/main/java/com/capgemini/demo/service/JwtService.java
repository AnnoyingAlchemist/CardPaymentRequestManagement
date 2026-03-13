package com.capgemini.demo.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

@Service
public class JwtService {

    public static final String SECRET = "d49fa20344c5928dc70dd3fca9c9b380";

    private Key getSignedKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public Claims verifySignatureAndExtractClaims(String token){
        return Jwts.parser()
                .setAllowedClockSkewSeconds(5) // tolerate 5s drift
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }



    public String extractUsername(String token){
        return verifySignatureAndExtractClaims(token).getSubject();
    }

    public Date getExpiration(String token){
        return verifySignatureAndExtractClaims(token).getExpiration();
    }


    public boolean isTokenExpired(String token){
        try {
            Date exp = getExpiration(token);   // this will throw if expired
            return exp.before(new Date());     // normal case (not thrown)
        } catch (ExpiredJwtException e) {
            return true;                       // token is already expired
        } catch (JwtException e) {
            return true;                       // invalid/tampered token => treat as expired
        }
    }

}
