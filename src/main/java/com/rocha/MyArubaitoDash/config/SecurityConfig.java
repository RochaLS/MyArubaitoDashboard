package com.rocha.MyArubaitoDash.config;


import com.rocha.MyArubaitoDash.repository.WorkerRepository;
import com.rocha.MyArubaitoDash.security.CustomAuthEntryPoint;
import com.rocha.MyArubaitoDash.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.server.Cookie;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.session.web.http.HttpSessionIdResolver;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/worker/add").permitAll()
                        .requestMatchers("api/worker/check-account").permitAll()
                        .requestMatchers("/password-reset/request").permitAll()
                        .requestMatchers("/password-reset/reset").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/api/validate-session").permitAll()
                        .requestMatchers("/test-gemini-connection").permitAll()
                        .requestMatchers("/process-image").permitAll()
                        .anyRequest().authenticated()
                )
//                .csrf().disable()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(customizer -> customizer
                        .authenticationEntryPoint(new CustomAuthEntryPoint())

                );

        return http.build();
    }


    @Bean
    public UserDetailsService userDetailsService(WorkerRepository workerRepository) {
        return new CustomUserDetailsService(workerRepository);
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