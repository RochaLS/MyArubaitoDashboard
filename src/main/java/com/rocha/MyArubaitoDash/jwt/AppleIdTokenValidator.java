package com.rocha.MyArubaitoDash.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class AppleIdTokenValidator {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public AppleIdTokenValidator() throws Exception {
        // Set up the Remote JWK Source
        RemoteJWKSet<SecurityContext> jwkSet = new RemoteJWKSet<>(
                new URL(APPLE_KEYS_URL),
                new DefaultResourceRetriever(5000, 5000) // Set timeout for fetching the keys
        );

        // Set up the JWT Processor
        jwtProcessor = new DefaultJWTProcessor<>();
        jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSet));
    }

    public JWTClaimsSet validateIdToken(String idToken, String clientId) throws Exception {
        try {
            // Parse the signed JWT
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            // Process the token and validate its signature
            JWTClaimsSet claims = jwtProcessor.process(signedJWT, null);

            // Validate required claims
            validateClaims(claims, clientId);

            return claims;
        } catch (Exception e) {
            throw new RuntimeException("ID Token validation failed: " + e.getMessage(), e);
        }
    }

    private void validateClaims(JWTClaimsSet claims, String clientId) {
        // Validate audience (your app's client ID)
        if (!claims.getAudience().contains(clientId)) {
            throw new IllegalArgumentException("Invalid audience: " + claims.getAudience());
        }

        // Validate issuer
        if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
            throw new IllegalArgumentException("Invalid issuer: " + claims.getIssuer());
        }

        // Validate expiration
        if (claims.getExpirationTime().before(new java.util.Date())) {
            throw new IllegalArgumentException("Token is expired");
        }

        // Optional: Add other validations (e.g., nonce, authorized party)
    }
}