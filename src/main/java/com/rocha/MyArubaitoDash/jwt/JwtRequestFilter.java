package com.rocha.MyArubaitoDash.jwt;

import com.rocha.MyArubaitoDash.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNullApi;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    // Dependencies for JWT processing and user details retrieval
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Constructor with dependency injection
     * @param customUserDetailsService Service to load user details by username(email in our case)
     * @param jwtTokenUtil Utility to validate and extract data from JWT tokens
     */
    @Autowired
    public JwtRequestFilter(CustomUserDetailsService customUserDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * Main filter method that intercepts each request to validate JWT tokens
     * @param request The HTTP request
     * @param response The HTTP response
     * @param filterChain The filter chain for additional filters
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Extract the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");
        String email = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extract the JWT token (remove the "Bearer " prefix)
            jwt = authorizationHeader.substring(7);
            try {
                email = jwtTokenUtil.extractEmail(jwt);
            } catch (Exception e) {
                logger.error("Unable to get JWT Token", e);
            }
        }

        // If email was successfully extracted and no authentication exists yet
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load the user details from the database
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // Validate the token against the email from user details
            if (jwtTokenUtil.validateToken(jwt, userDetails.getUsername())) {
                // Create an authentication token with user details and authorities
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Add request details to the authentication
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}