package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla CommentDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface CommentRequired { }
