package com.mamadou.sutura.webhook.repository;

import com.mamadou.sutura.webhook.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByProviderAndExternalEventId(String provider, String externalEventId);
}
