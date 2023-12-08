package com.mastercom.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

    @Value("${db.passwords.encode.password}")
    private String secretValue;

    @Value("${db.passwords.encode.salt}")
    private String saltValue;


    private TextEncryptor getTextEncryptor() {
        final String secret = new String(Hex.encode(secretValue.getBytes())); // convert to hex
        final String salt = new String(Hex.encode(saltValue.getBytes()));
        return Encryptors.text(secret, salt);
    }


    public String encrypt(String data) {
        return getTextEncryptor().encrypt(data);
    }

    public String decrypt(String encryptedData) {
        return getTextEncryptor().decrypt(encryptedData);
    }
}
