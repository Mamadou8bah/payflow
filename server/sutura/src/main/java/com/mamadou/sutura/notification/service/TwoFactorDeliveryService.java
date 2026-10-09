package com.mamadou.sutura.notification.service;

import com.mamadou.sutura.common.Exception.InvalidTokenException;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TwoFactorDeliveryService {

    private final SmsSender smsSender;
    private final EmailSender emailSender;

    public void sendCode(User user, String code) {
        if (user.getRole() == Role.MERCHANT || user.getRole() == Role.DEVELOPER || user.getRole() == Role.ADMIN) {
            sendEmailCode(user, code);
            return;
        }
        sendSmsCode(user, code);
    }

    private void sendSmsCode(User user, String code) {
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()) {
            throw new InvalidTokenException("Customer phone number is required for 2FA");
        }
        smsSender.send(user.getPhoneNumber(), "Your Sutura verification code is " + code + ". It expires in 5 minutes.");
    }

    private void sendEmailCode(User user, String code) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new InvalidTokenException("Merchant email is required for 2FA");
        }
        emailSender.send(
                user.getEmail(),
                "Your Sutura verification code",
                "Your Sutura verification code is " + code + ". It expires in 5 minutes."
        );
    }
}
