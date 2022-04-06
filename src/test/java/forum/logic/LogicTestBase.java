package forum.logic;

import forum.TestBase;
import forum.databases.Database;
import forum.services.Authenticator;
import forum.services.Clock;
import forum.services.Emailer;
import forum.validators.Validator;

import static org.mockito.Mockito.mock;

public abstract class LogicTestBase extends TestBase {
    protected final Database database = mock(Database.class);
    protected long time = 0;
    protected final Clock clock = () -> time;
    protected final Validator validator = mock(Validator.class);
    protected final Authenticator authenticator = mock(Authenticator.class);
    protected final Emailer emailer = mock(Emailer.class);
}
