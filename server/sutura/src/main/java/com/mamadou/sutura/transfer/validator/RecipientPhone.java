package com.mamadou.sutura.transfer.validator;

import com.mamadou.sutura.wallet.exception.WalletOperationException;
import java.util.List;

public final class RecipientPhone {
    private RecipientPhone() {}

    public static String normalize(String input) {
        String phone = input == null ? "" : input.trim().replaceAll("[\\s().-]", "");
        if (phone.startsWith("00")) phone = "+" + phone.substring(2);
        if (phone.matches("[0-9]{7}")) phone = "+220" + phone;
        else if (phone.matches("[0-9]{8,15}")) phone = "+" + phone;
        if (!phone.matches("\\+[1-9][0-9]{7,14}"))
            throw new WalletOperationException("Enter a valid recipient phone number, including its country code");
        return phone;
    }

    public static List<String> storedForms(String normalized) {
        String digits = normalized.substring(1);
        return normalized.startsWith("+220") && digits.length() == 10
                ? List.of(normalized, digits, "00" + digits, digits.substring(3))
                : List.of(normalized, digits, "00" + digits);
    }
}
