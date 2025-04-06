package DA.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Áp dụng cho tất cả endpoint
                        .allowedOrigins("http://localhost:3000","https://foxhound-sharing-mackerel.ngrok-free.app/") // Cho phép frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Các method HTTP được phép
                        .allowedHeaders("*") // Cho phép tất cả header
                        .allowCredentials(true); // Nếu có sử dụng cookie hoặc header Authorization
            }
        };
    }
}
