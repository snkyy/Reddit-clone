package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla OptionalLoginDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginOptional { }
