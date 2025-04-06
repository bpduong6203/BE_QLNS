// src/main/java/DA/backend/SecurityConfig.java
package DA.backend;

import DA.backend.security.JwtAuthenticationFilter;
import DA.backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF
                .cors(cors -> cors
                        .configurationSource(request -> {
                            CorsConfiguration config = new CorsConfiguration();
                            config.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
                            config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                            // Thêm user-id vào allowedHeaders
                            config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "user-id"));
                            config.setAllowCredentials(true);
                            return config;
                        })
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**",
                                "/api/**",
                                "/api/auth/login", // Bỏ khoảng trắng ở đây
                                "/api/meetings/**",
                                "/api/activities/**",
                                "/api/user/add",
                                "/api/user/add/images",
                                "/api/user/**",
                                "/api/question/**",
                                "/api/user/update",
                                "/api/user/profile",
                                "/api/user/{id}/image",
                                "/api/department/add",
                                "/api/department/update",
                                "/api/department/listUser",
                                "/api/department/listDepartment",
                                "/api/department/addUser",
                                "/api/department",
                                "/api/department/listDepartmentUser",
                                "/api/user/export/excel",
                                "/api/position/**",
                                "/api/file/**",
                                "/api/evaluate/**",
                                "/api/questions/**",
                                "/api/questionSet/**",
                                "/api/timeEvaluateRole/**",
                                "/api/userEvaluate/**",
                                "/api/leave/**",
                                "/api/leave/manager/**",
                                "/api/attendance/**",
                                "/api/attendance/manager/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
