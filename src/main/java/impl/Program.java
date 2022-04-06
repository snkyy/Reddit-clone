package impl;

import forum.ForumAppFactory;

// uruchomienie aplikacji z implementacjami w tym pakiecie
class Program {
    public static void main(String[] args) {
        var database = new SQLiteDatabase();
        var clock = new SystemClock();
        var validator = new DefaultValidator();
        var authenticator = new SHA1Authenticator();
        var emailer = new FakeEmailer();
        var server = new JettyServer();
        var sessionManager = new SimpleSessionManager();
        var templateProcessor = new FreeMarkerTemplateProcessor();

        var factory = new ForumAppFactory(
                database,
                clock,
                validator,
                authenticator,
                emailer,
                server,
                sessionManager,
                templateProcessor);

        factory.newForumApp().run();
    }
}
