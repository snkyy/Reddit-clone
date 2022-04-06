package forum.views;

import lombok.Value;

@Value
public class RegisterView {
    public boolean nameInvalid;
    public boolean emailInvalid;
    public boolean nameTaken;
    public boolean passwordsMismatch;
}
