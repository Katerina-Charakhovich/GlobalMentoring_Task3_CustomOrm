package com.epam.customorm.config;

import com.epam.customorm.dao.UserRepository;
import com.epam.customorm.service.UserConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.epam.customorm")
public class DaoConfig {

    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }

    @Bean
    public UserConverter userConverter() {
        return new UserConverter();
    }
}
