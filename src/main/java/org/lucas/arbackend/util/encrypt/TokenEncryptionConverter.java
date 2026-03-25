package org.lucas.arbackend.util.encrypt;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.lucas.arbackend.util.EncryptionUtils;
import org.springframework.beans.factory.annotation.Value;

@Converter
public class TokenEncryptionConverter implements AttributeConverter<String, String> {

    @Value("${encryption.key")
    private String encryptionKey;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || encryptionKey == null) return null;
        return EncryptionUtils.encrypt(attribute, encryptionKey);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null | encryptionKey == null) return null;
        return EncryptionUtils.decrypt(dbData, encryptionKey);
    }

}
