package com.example.vaiechotestbot2.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot_name}")
    String botName;
    @Value("${bot_key}")
    String token;

    @Value("${default_delay}")
    Long delay;
}
