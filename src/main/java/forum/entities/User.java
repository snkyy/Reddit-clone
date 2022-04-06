package forum.entities;

import lombok.Value;

import java.util.Optional;

// dane użytkownika (tylko do odczytu)
@Value
public class User {
    public long id;
    public String name;
    public Optional<String> email;
    public long joinedTime;
    public byte[] passwordHash;
    public byte[] passwordSalt;
    public long points;
}
