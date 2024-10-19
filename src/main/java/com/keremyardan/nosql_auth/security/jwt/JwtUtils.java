package com.keremyardan.nosql_auth.security.jwt;

import com.keremyardan.nosql_auth.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.security.Key;
import java.util.Base64;
import java.util.Date;


@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${keremyardan.app.jwtSecret}")
    private String jwtSecret;

    @Value("${keremyardan.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String generateJwtToken (Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();


        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(),SignatureAlgorithm.ES256)
                .compact();
    }

    public String getUserNameFromJwtToken (String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken (String authToken){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        }catch (MalformedJwtException e) {
            logger.error("Invalid JWT Token: {}", e.getMessage());
        }catch (ExpiredJwtException e) {
            logger.error("JWT Token is Expired: {}", e.getMessage());
        }catch (UnsupportedJwtException e) {
            logger.error("JWT Token is Unsupported: {}", e.getMessage());
        }catch (IllegalArgumentException e) {
            logger.error("JWT Claims String is Empty: {}", e.getMessage());
        }
        return false;
    }
}
