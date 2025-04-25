package com.rocha.MyArubaitoDash.config;


import com.rocha.MyArubaitoDash.jwt.JwtRequestFilter;
import com.rocha.MyArubaitoDash.jwt.JwtTokenUtil;
import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.security.CustomAuthEntryPoint;
import com.rocha.MyArubaitoDash.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   UserDetailsService customUserDetailsService,
                                                   JwtTokenUtil jwtTokenUtil) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/worker/add").permitAll()
                        .requestMatchers("/api/worker/check-account").permitAll()
                        .requestMatchers("/api/password-reset/request").permitAll()
                        .requestMatchers("/api/password-reset/reset").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/api/validate-session").permitAll()
//                        .requestMatchers("/test-gemini-connection").permitAll()
//                        .requestMatchers("/process-image").permitAll()
                        .requestMatchers("/api/mobile/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, exception) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication failed\"}");
                        })
                )
                .addFilterBefore(new JwtRequestFilter(customUserDetailsService, jwtTokenUtil),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    @Order(2)
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((authorize) -> authorize
//                        .requestMatchers("/api/worker/add").permitAll()
//                        .requestMatchers("api/worker/check-account").permitAll()
//                        .requestMatchers("/password-reset/request").permitAll()
//                        .requestMatchers("/password-reset/reset").permitAll()
//                        .requestMatchers("/login").permitAll()
//                        .requestMatchers("/api/validate-session").permitAll()
//                        .requestMatchers("/test-gemini-connection").permitAll()
//                        .requestMatchers("/process-image").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .csrf().disable()
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
//                .httpBasic(Customizer.withDefaults())
//                .exceptionHandling(customizer -> customizer
//                        .authenticationEntryPoint(new CustomAuthEntryPoint())
//
//                );
//
//        return http.build();
//    }
//
//    // JWT Configuration for Mobile API endpoints
//    @Bean
//    @Order(1)
//    public SecurityFilterChain MobileApiSecurityFilterChain(HttpSecurity http, UserDetailsService customUserDetailsService, JwtTokenUtil jwtTokenUtil) throws Exception {
//        http
//                .securityMatcher(request -> {
//                    String path = request.getServletPath();
//                    return path.startsWith("/api/mobile/") || path.startsWith("/api/income/") || path.startsWith("/api/shift/");
//                })
//                .authorizeHttpRequests(authorize -> authorize
//                        .requestMatchers("/api/mobile/auth/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .csrf(csrf -> csrf.disable())
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .exceptionHandling(handler -> handler
//                        .authenticationEntryPoint((request, response, exception) -> {
//                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                            response.setContentType("application/json");
//                            response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication failed\"}");
//                        })
//                )
//                .addFilterBefore(new JwtRequestFilter(customUserDetailsService, jwtTokenUtil),
//                        UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }


    @Bean
    public UserDetailsService userDetailsService(WorkerRepository workerRepository) {
        return new CustomUserDetailsService(workerRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("prod")
    public CookieSameSiteSupplier cookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone().whenHasName("JSESSIONID");
    }

    @Bean
    @Profile("local")
    public CookieSameSiteSupplier noOpCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax().whenHasName("JSESSIONID");
    }

    @Bean
    @Profile("prod")
    public HttpSessionIdResolver httpSessionIdResolver() {
        CookieHttpSessionIdResolver resolver = new CookieHttpSessionIdResolver();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setUseSecureCookie(true); // Set the Secure attribute
        cookieSerializer.setDomainName(".myarubaito.com");
        resolver.setCookieSerializer(cookieSerializer);

        return resolver;
    }



}