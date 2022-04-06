package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// informacja dla ControllerMethod, że kontroler ma mieć type Action
@Retention(RetentionPolicy.RUNTIME)
public @interface Action { }
