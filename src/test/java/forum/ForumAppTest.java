package forum;

import forum.logic.Forum;
import forum.services.Emailer;
import forum.web.Server;
import forum.web.SessionManager;
import forum.web.TemplateProcessor;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class ForumAppTest {
    @Test
    public void testRun() {
        var forum = mock(Forum.class);
        var emailer = mock(Emailer.class);
        var server = mock(Server.class);
        var sessionManager = mock(SessionManager.class);
        var templateProcessor = mock(TemplateProcessor.class);
        var app = new ForumApp(forum, emailer, server, sessionManager, templateProcessor);

        app.run();

        verify(server).run(any());
    }
}
