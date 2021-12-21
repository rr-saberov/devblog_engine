package ru.spring.app.engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    private final StorageProperties storageProperties = new StorageProperties();

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        final String uploadPath = storageProperties.getLocation();
        registry
                .addResourceHandler(String.format("/%s/**", uploadPath))
                .addResourceLocations(String.format("file:%s/", uploadPath));
    }
}
