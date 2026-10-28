package dev.mmatokovic.pantrio.infra.security;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                   AuthenticationEntryPoint authenticationEntryPoint,
                                   AccessDeniedHandler accessDeniedHandler) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
                authz.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                authz.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                authz.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                authz.requestMatchers("/actuator/prometheus").hasRole("METRICS");
                authz.anyRequest().hasRole("ADMIN");
            })
            .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(authenticationEntryPoint))
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .logout(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    AuthenticationEntryPoint authenticationEntryPoint(JsonMapper jsonMapper) {
        return new CustomAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler(JsonMapper jsonMapper) {
        return new CustomAccessDeniedHandler(jsonMapper);
    }

    @Bean
    UserDetailsService userDetailsService(SecurityUsersProperties securityUsersProperties, PasswordEncoder passwordEncoder) {
        var admin = User.builder()
            .username("user")
            .password(passwordEncoder.encode(securityUsersProperties.user().password()))
            .roles("ADMIN")
            .build();
        var prometheus = User.builder()
            .username("prometheus")
            .password(passwordEncoder.encode(securityUsersProperties.prometheus().password()))
            .roles("METRICS")
            .build();
        return new InMemoryUserDetailsManager(prometheus, admin);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
