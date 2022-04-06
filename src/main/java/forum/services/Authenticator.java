package forum.services;

import lombok.Value;

// interfejs do funkcji hashującej i sprawdzającej hasła
public interface Authenticator {
    HashedPassword hashPassword(String password); // hashuje hasło z losowo wygenerowaną solą
    boolean authenticate(String password, HashedPassword hashedPassword); // sprawdza hasło na podstawie zapisanego hashu i soli

    @Value
    class HashedPassword {
        public byte[] hash;
        public byte[] salt;
    }
}
