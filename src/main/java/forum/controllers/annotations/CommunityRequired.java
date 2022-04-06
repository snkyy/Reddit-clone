package forum.controllers.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// adnotacja dla CommunityDecorator
@Retention(RetentionPolicy.RUNTIME)
public @interface CommunityRequired { }
