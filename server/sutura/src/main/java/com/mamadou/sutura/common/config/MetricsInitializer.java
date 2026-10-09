package com.mamadou.sutura.common.config;

import com.mamadou.sutura.common.metrics.SuturaMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Initializes Sutura metrics on application startup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsInitializer {

    private final SuturaMetricsService metricsService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeMetrics() {
        log.info("Initializing Sutura metrics...");
        metricsService.initialize();
        log.info("Sutura metrics initialized successfully");
    }
}
