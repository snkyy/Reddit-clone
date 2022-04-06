package forum.exceptions;

// wyjątek rzucany przez forum.logic.Communities.create()
public class CommunityCreateException extends Exception {
    public enum Cause {
        NameTaken,
        NameInvalid
    }

    public final Cause cause;

    public CommunityCreateException(Cause cause) {
        super(cause.name());
        this.cause = cause;
    }
}
