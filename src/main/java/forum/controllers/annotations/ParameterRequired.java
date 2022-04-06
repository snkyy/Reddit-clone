package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla ParameterDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterRequired {
    // nazwy parametrów oddzielone przecinkami
    @SuppressWarnings("unused")
    String value();
}
