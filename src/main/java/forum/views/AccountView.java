package forum.views;

import forum.entities.User;
import lombok.Value;

@Value
public class AccountView {
    public User user;
    public boolean emailInvalid;
    public boolean passwordsMismatch;
}
