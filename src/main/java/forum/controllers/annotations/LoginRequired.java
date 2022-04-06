package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla LoginDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired { }
