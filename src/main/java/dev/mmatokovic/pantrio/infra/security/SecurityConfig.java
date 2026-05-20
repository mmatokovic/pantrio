package dev.mmatokovic.pantrio.infra.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${security.user.password}")
    private String userPassword;

    @Value("${security.prometheus.password}")
    private String prometheusPassword;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(authz -> {
                authz.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
                authz.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                authz.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                authz.requestMatchers("/actuator/prometheus").hasRole("METRICS");
                authz.anyRequest().authenticated();
            })
            .httpBasic(httpBasic ->
                httpBasic.authenticationEntryPoint(authenticationEntryPoint())
            )
            .logout(AbstractHttpConfigurer::disable)
            .build();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("user").password(passwordEncoder().encode(userPassword))
            .roles("ADMIN")
            .build();

        UserDetails prometheus = User.builder()
            .username("prometheus").password(passwordEncoder().encode(prometheusPassword))
            .roles("METRICS")
            .build();

        return new InMemoryUserDetailsManager(prometheus, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
