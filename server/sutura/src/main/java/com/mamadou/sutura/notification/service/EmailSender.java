package com.mamadou.sutura.notification.service;

public interface EmailSender {
    void send(String email, String subject, String body);
}
