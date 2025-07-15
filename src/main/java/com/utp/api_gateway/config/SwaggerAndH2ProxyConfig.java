package com.utp.api_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SwaggerAndH2ProxyConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Redirección automática de Swagger de auth-service
        registry.addRedirectViewController("/auth/swagger-ui", "http://localhost:8081/swagger-ui/index.html");
        registry.addRedirectViewController("/auth/swagger-ui/", "http://localhost:8081/swagger-ui/index.html");

        // Redirección automática de Swagger de course-service
        registry.addRedirectViewController("/cursos/swagger-ui", "http://localhost:8082/swagger-ui/index.html");
        registry.addRedirectViewController("/cursos/swagger-ui/", "http://localhost:8082/swagger-ui/index.html");

        // H2 console de course-service
        registry.addRedirectViewController("/cursos/h2", "http://localhost:8084/h2-console");
        registry.addRedirectViewController("/cursos/h2/", "http://localhost:8084/h2-console");
    }
}