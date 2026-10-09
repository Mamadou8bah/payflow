package com.mamadou.sutura.notification.service;

public interface SmsSender {
    void send(String phoneNumber, String message);
}
