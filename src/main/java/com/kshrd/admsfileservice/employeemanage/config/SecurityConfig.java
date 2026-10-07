package com.kshrd.admsfileservice.employeemanage.config;

import com.kshrd.admsfileservice.employeemanage.security.AppUserDetailsService;
import com.kshrd.admsfileservice.employeemanage.security.JsonAuthHandlers;
import com.kshrd.admsfileservice.employeemanage.security.JwtAuthenticationFilter;
import com.kshrd.admsfileservice.employeemanage.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtService jwtService;
    private final AppUserDetailsService appUserDetailsService;
    private final JsonAuthHandlers jsonAuthHandlers;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, appUserDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthHandlers)
                        .accessDeniedHandler(jsonAuthHandlers))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/swagger-ui-theme.css"
                        ).permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/settings/**").hasAuthority("settings:view")
                        .requestMatchers("/api/v1/settings/**").hasAuthority("settings:write")

                        .requestMatchers(HttpMethod.GET, "/api/v1/payrolls/**").hasAuthority("payroll:view")
                        .requestMatchers("/api/v1/payrolls/**").hasAuthority("payroll:write")

                        .requestMatchers(HttpMethod.POST, "/api/v1/employees").hasAuthority("employees:write")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/employees/**").hasAuthority("employees:write")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/employees/**").hasAuthority("employees:write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/employees/**").hasAuthority("employees:write")

                        .requestMatchers(HttpMethod.PATCH, "/api/v1/leaves/**").hasAuthority("leaves:decide")

                        .requestMatchers(HttpMethod.POST, "/api/v1/organization/**").hasAuthority("organization:write")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/organization/**").hasAuthority("organization:write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/organization/**").hasAuthority("organization:write")

                        .requestMatchers(HttpMethod.POST, "/api/v1/announcements/**").hasAuthority("announcements:write")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/announcements/**").hasAuthority("announcements:write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/announcements/**").hasAuthority("announcements:write")

                        .requestMatchers(HttpMethod.POST, "/api/v1/documents/**").hasAuthority("documents:write")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/documents/**").hasAuthority("documents:write")

                        .requestMatchers(HttpMethod.POST, "/api/v1/performance-reviews/**").hasAuthority("performance:write")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
