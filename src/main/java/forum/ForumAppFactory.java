package forum;

import forum.databases.Database;
import forum.logic.ForumFactory;
import forum.services.Authenticator;
import forum.services.Clock;
import forum.services.Emailer;
import forum.validators.Validator;
import forum.web.Server;
import forum.web.SessionManager;
import forum.web.TemplateProcessor;
import lombok.AllArgsConstructor;

// pomocnicza klasa konstruująca Forum i ForumApp
// wstrzykując do nich odpowiednie zależności;
// są to wszystkie zależności potrzebne do uruchomienia tej aplikacji
@AllArgsConstructor
public class ForumAppFactory {
    private final Database database;
    private final Clock clock;
    private final Validator validator;
    private final Authenticator authenticator;
    private final Emailer emailer;
    private final Server server;
    private final SessionManager sessionManager;
    private final TemplateProcessor templateProcessor;

    public ForumApp newForumApp() {
        var factory = new ForumFactory(database, clock, validator, authenticator, emailer);
        var forum = factory.newForum();
        return new ForumApp(forum, emailer, server, sessionManager, templateProcessor);
    }
}
