package br.com.webbudget.infrastructure.spring.security;

import br.com.webbudget.vaadin.views.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(jsr250Enabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Spring Security 7 (Spring Boot 4) defaults to DelegatingSecurityContextRepository,
        // which combines HttpSession + request-attribute storage. Vaadin's request wrapping
        // can prevent the session fallback from firing, losing the authentication context
        // between UIDL requests. Pinning to HttpSessionSecurityContextRepository restores
        // the pre-SS7 session-only behaviour and keeps the user logged in across navigation.
        return http
                .securityContext(ctx -> ctx
                        .securityContextRepository(new HttpSessionSecurityContextRepository()))
                .sessionManagement(session -> session
                        .sessionFixation().changeSessionId()
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .with(VaadinSecurityConfigurer.vaadin(), configurer -> configurer.loginView(LoginView.class))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
