package forum.logic;

import forum.exceptions.UserRegisterException;
import forum.validators.UserValidator;
import forum.services.Authenticator;
import forum.services.Clock;
import forum.services.Emailer;
import forum.databases.UsersDatabase;
import forum.entities.*;
import forum.forms.UserRegisterForm;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static forum.exceptions.UserRegisterException.Cause.*;

// logika biznesowa związana z użytkownikami
@AllArgsConstructor
public class Users {
    private final UsersDatabase database;
    private final Clock clock;
    private final UserValidator validator;
    private final Authenticator authenticator;
    private final Emailer emailer;

    public static final long feedMaxAgeDays = 7;
    public static final long feedMaxCount = 100;

    public Optional<User> get(long id) {
        return database.getUser(id);
    }

    public Optional<User> getByName(String name) {
        return database.getUserByName(name);
    }

    public List<Post> getPosts(User user) {
        return database.getUserPosts(user.id);
    }

    public List<Comment> getComments(User user) {
        return database.getUserComments(user.id);
    }

    public List<Subscriber> getSubscriptions(User user) {
        return database.getUserSubscriptions(user.id);
    }

    public List<PostVote> getPostVotes(User user) {
        return database.getUserPostVotes(user.id);
    }

    public List<CommentVote> getCommentVotes(User user) {
        return database.getUserCommentVotes(user.id);
    }

    public List<Post> getFeed(User user) {
        var time = clock.time();
        var newerThan = time - feedMaxAgeDays * 24 * 3600;
        var posts = database.getUserSubscriptionsPosts(user.id, newerThan);

        ToDoubleFunction<Post> scoreFunction = p -> -Communities.postScore(p.points, time - p.sentTime);
        posts = new ArrayList<>(posts);
        posts.sort(Comparator.comparingDouble(scoreFunction));

        return posts.stream().limit(feedMaxCount).collect(Collectors.toList());
    }

    public User register(UserRegisterForm form) throws UserRegisterException {
        if (!validator.validateUserName(form.name))
            throw new UserRegisterException(NameInvalid);
        if (form.email.isPresent() && !validator.validateEmail(form.email.get()))
            throw new UserRegisterException(EmailInvalid);
        if (database.getUserByName(form.name).isPresent())
            throw new UserRegisterException(NameTaken);

        var time = clock.time();
        var hashedPassword = authenticator.hashPassword(form.password);

        var user = database.newUser(form.name, form.email, time, hashedPassword.hash, hashedPassword.salt);
        emailer.emailUser(user, "registered");
        return user;
    }

    public boolean authenticate(User user, String password) {
        var hashedPassword = new Authenticator.HashedPassword(user.passwordHash, user.passwordSalt);
        return authenticator.authenticate(password, hashedPassword);
    }

    public boolean setEmail(User user, Optional<String> email) {
        if (email.isPresent() && !validator.validateEmail(email.get()))
            return false;
        database.setUserEmail(user.id, email);
        emailer.emailUser(user, "email-updated");
        return true;
    }

    public void setPassword(User user, String password) {
        var hashedPassword = authenticator.hashPassword(password);
        database.setUserPassword(user.id, hashedPassword.hash, hashedPassword.salt);
        emailer.emailUser(user, "password-updated");
    }

    public void addPoints(User user, long points) {
        database.addUserPoints(user.id, points);
    }

    public void delete(User user) {
        database.deleteUser(user.id);
        emailer.emailUser(user, "deleted");
    }
}
