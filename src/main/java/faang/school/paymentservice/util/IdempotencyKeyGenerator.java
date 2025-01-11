package faang.school.paymentservice.util;

import faang.school.paymentservice.model.dto.PendingOperationDto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class IdempotencyKeyGenerator {

    public static String generateKey(PendingOperationDto operation) {
        String rawKey = String.join("|",
                operation.getSourceAccountId().toString(),
                operation.getTargetAccountId().toString(),
                operation.getAmount().toString(),
                operation.getCurrency(),
                operation.getCategory(),
                operation.getClearScheduledAt().toString()
        );

        return hashToBase64(rawKey);
    }

    private static String hashToBase64(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}
