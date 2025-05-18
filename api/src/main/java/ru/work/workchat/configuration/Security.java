package ru.work.workchat.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import ru.work.workchat.model.dto.OperationResultDTO;
import ru.work.workchat.service.UserDetailsServiceImpl;

import java.util.List;

@AllArgsConstructor
@Configuration
@EnableMethodSecurity
public class Security {
    UserDetailsServiceImpl userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityContextRepository securityContextRepository(){
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOriginPatterns(List.of("http://localhost", "http://localhost:3000"));
                    corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
                    corsConfiguration.setAllowedHeaders(List.of("*"));
                    corsConfiguration.setAllowCredentials(true);
                    return corsConfiguration;
                }))
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/login", "/register").anonymous()
                                .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) -> {
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    response.setCharacterEncoding("UTF-8");
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    objectMapper.writeValue(response.getWriter(), new OperationResultDTO(null, "Нет доступа"));
                                })
                        .authenticationEntryPoint((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ObjectMapper objectMapper = new ObjectMapper();
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            objectMapper.writeValue(response.getWriter(), new OperationResultDTO(null, "Нет аутентификации"));
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .logout(logout ->
                        logout.logoutUrl("/logout").logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            ObjectMapper objectMapper = new ObjectMapper();
                            if(authentication != null && authentication.isAuthenticated()){
                                response.setStatus(HttpServletResponse.SC_OK);
                                objectMapper.writeValue(response.getWriter(), new OperationResultDTO("Выход выполнен", null));
                            } else {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                objectMapper.writeValue(response.getWriter(), new OperationResultDTO(null, "Вы не аутентифицирован"));
                            }
                        }).invalidateHttpSession(true));
        return http.build();
    }
}
