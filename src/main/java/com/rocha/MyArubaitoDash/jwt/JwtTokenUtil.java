package com.rocha.MyArubaitoDash.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    @Value("${JWT_SECRET}")
    private String secretKey;

    @Value("${JWT_EXPIRATION}")
    private long expiration;

    // Creates key to be sued to sign the JWT
    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, int id) {
        Map<String, Object> claims = new HashMap<>();

        Date now = new Date();
        claims.put("iat", now.getTime() / 1000); // Issued at time
        claims.put("exp", (now.getTime() + (60 * 60 * 1000)) / 1000); // Expires in 30 minutes
        claims.put("iss", "Baito"); // Issuer
        claims.put("sub", email); // Subject (email as identifier)
        claims.put("worker_id", id);

        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, String email) {
        final String tokenEmail = extractEmail(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract a specific claim from a JWT token.
     *
     * This method uses Java's functional programming capabilities to provide a flexible
     * way to extract any type of claim from the JWT payload. It handles the token parsing
     * and validation once, then allows the caller to specify exactly which piece of data
     * they want from the claims.
     *
     * @param <T> The type of the claim value to be extracted
     * @param token The JWT token string to extract claims from
     * @param claimsResolver A function that takes the Claims object and returns the desired value
     * @return The extracted claim value of type T
     *
     * Example usages:
     *   String username = extractClaim(token, Claims::getSubject);
     *   Date expiration = extractClaim(token, Claims::getExpiration);
     *   String customClaim = extractClaim(token, claims -> claims.get("customField", String.class));
     *
     * This approach provides type safety while reducing code duplication for different claim extractions.
     */

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
