package forum.logic;

import forum.databases.Database;
import forum.validators.Validator;
import forum.services.Authenticator;
import forum.services.Clock;
import forum.services.Emailer;
import lombok.AllArgsConstructor;

// pomocnicza klasa konstruująca Users, Communities, Posts, Comments i Forum
// wstrzykując do nich odpowiednie zależności
@AllArgsConstructor
public class ForumFactory {
    private final Database database;
    private final Clock clock;
    private final Validator validator;
    private final Authenticator authenticator;
    private final Emailer emailer;

    public Forum newForum() {
        var users = new Users(database, clock, validator, authenticator, emailer);
        var communities = new Communities(database, clock, validator, emailer);
        var posts = new Posts(database, clock, emailer, users);
        var comments = new Comments(database, clock, emailer, users);
        return new Forum(users, communities, posts, comments);
    }
}
