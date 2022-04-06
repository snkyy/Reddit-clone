package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla NumberDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface NumberRequired {
    // nazwy parametrów oddzielone przecinkami
    @SuppressWarnings("unused")
    String value();
}
