package impl;

import forum.services.Authenticator;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

// hashowanie i sprawdzanie haseł za pomocą algorytmu SHA-1
public class SHA1Authenticator implements Authenticator {
    private final Random random = new Random();

    private MessageDigest digest() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HashedPassword hashPassword(String password) {
        var salt = new byte[8];
        random.nextBytes(salt);

        var digest = digest();
        digest.update(password.getBytes());
        digest.update(salt);
        var hash = digest.digest();

        return new HashedPassword(hash, salt);
    }

    @Override
    public boolean authenticate(String password, HashedPassword hashedPassword) {
        var digest = digest();
        digest.update(password.getBytes());
        digest.update(hashedPassword.salt);
        var hash = digest.digest();

        return Arrays.equals(hash, hashedPassword.hash);
    }
}
