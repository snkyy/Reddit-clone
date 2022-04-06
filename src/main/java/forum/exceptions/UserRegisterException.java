package forum.exceptions;

// wyjątek rzucany przez forum.logic.Users.register()
public class UserRegisterException extends Exception {
    public enum Cause {
        NameTaken,
        NameInvalid,
        EmailInvalid
    }

    public final Cause cause;

    public UserRegisterException(Cause cause) {
        super(cause.name());
        this.cause = cause;
    }
}
