package com.mamadou.sutura.common.config;

import com.mamadou.sutura.common.ratelimit.RateLimitingService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RateLimitingService.RateLimitConfig.class)
public class RateLimitConfiguration {
}
