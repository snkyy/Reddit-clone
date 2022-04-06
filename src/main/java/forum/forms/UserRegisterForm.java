package forum.forms;

import lombok.Value;

import java.util.Optional;

// dane przekazywane do forum.logic.Users.register()
@Value
public class UserRegisterForm {
    public String name;
    public Optional<String> email;
    public String password;
}
