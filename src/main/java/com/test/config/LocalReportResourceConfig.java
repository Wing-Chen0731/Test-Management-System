package com.test.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class LocalReportResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/reports/jacoco/**")
                .addResourceLocations(reportLocation("jacoco"));

        registry.addResourceHandler("/reports/allure/**")
                .addResourceLocations(reportLocation("allure-maven-plugin"));
    }

    private String reportLocation(String reportDirectory) {
        return Path.of("target", "site", reportDirectory)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
    }
}
