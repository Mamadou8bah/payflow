package com.mamadou.sutura.webhook.sentinel;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component @Data
@ConfigurationProperties(prefix="sutura.sentinel-webhook")
public class SentinelWebhookProperties {
    private String signingSecret = "";
    private String tenantCode = "";
}
