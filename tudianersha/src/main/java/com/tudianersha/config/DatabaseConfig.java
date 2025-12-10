package com.tudianersha.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.tudianersha.repository")
public class DatabaseConfig {
    // Database configuration beans can be defined here if needed
}