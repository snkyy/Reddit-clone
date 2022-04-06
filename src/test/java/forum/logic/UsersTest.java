package forum.logic;

import forum.entities.Post;
import forum.entities.User;
import forum.forms.UserRegisterForm;
import forum.exceptions.UserRegisterException;
import forum.services.Authenticator;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsersTest extends LogicTestBase {
    private final Users users = new Users(database, clock, validator, authenticator, emailer);

    @Test
    public void testGet() {
        var user = mockUser();
        when(database.getUser(user.id)).thenReturn(Optional.of(user));

        var result1 = users.get(user.id);
        var result2 = users.get(user.id + 1);

        assertThat(result1).hasValue(user);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetByName() {
        var user = mockUser();
        when(database.getUserByName(user.name)).thenReturn(Optional.of(user));

        var result1 = users.getByName(user.name);
        var result2 = users.getByName(user.name + "1");

        assertThat(result1).hasValue(user);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetPosts() {
        var user = mockUser();
        var post1 = mockPost(user);
        var post2 = mockPost(user);
        when(database.getUserPosts(user.id)).thenReturn(List.of(post1, post2));

        var result = users.getPosts(user);

        assertThat(result).containsExactly(post1, post2);
    }

    @Test
    public void testGetComments() {
        var user = mockUser();
        var comment1 = mockComment(user);
        var comment2 = mockComment(user);
        when(database.getUserComments(user.id)).thenReturn(List.of(comment1, comment2));

        var result = users.getComments(user);

        assertThat(result).containsExactly(comment1, comment2);
    }

    @Test
    public void testGetSubscriptions() {
        var user = mockUser();
        var subscriber1 = mockSubscriber(user);
        var subscriber2 = mockSubscriber(user);
        when(database.getUserSubscriptions(user.id)).thenReturn(List.of(subscriber1, subscriber2));

        var result = users.getSubscriptions(user);

        assertThat(result).containsExactly(subscriber1, subscriber2);
    }

    @Test
    public void testGetPostVotes() {
        var user = mockUser();
        var vote1 = mockPostVote(user);
        var vote2 = mockPostVote(user);
        when(database.getUserPostVotes(user.id)).thenReturn(List.of(vote1, vote2));

        var result = users.getPostVotes(user);

        assertThat(result).containsExactly(vote1, vote2);
    }

    @Test
    public void testGetCommentVotes() {
        var user = mockUser();
        var vote1 = mockCommentVote(user);
        var vote2 = mockCommentVote(user);
        when(database.getUserCommentVotes(user.id)).thenReturn(List.of(vote1, vote2));

        var result = users.getCommentVotes(user);

        assertThat(result).containsExactly(vote1, vote2);
    }

    private void implGetUserSubscriptionsPosts(User user, List<Post> posts) {
        Answer<List<Post>> answer = inv -> {
            var newerThan = (long) inv.getArgument(1);
            Predicate<Post> predicate = p -> p.sentTime > newerThan;
            return posts.stream().filter(predicate).collect(Collectors.toList());
        };

        when(database.getUserSubscriptionsPosts(eq(user.id), anyLong())).then(answer);
    }

    @Test
    public void testGetFeed() {
        time = 24 * 3600 * Users.feedMaxAgeDays;
        var user = mockUser();
        var minPoints = -100;
        var maxPoints = 10000;

        var posts = new ArrayList<Post>();
        var random = new Random(0);
        for (var i = 0; i < Users.feedMaxCount + 1; i++) {
            var sentTime = 1 + random.nextInt((int)time);
            var points = minPoints + random.nextInt(maxPoints - minPoints);
            posts.add(mockPost(sentTime, points));
        }

        implGetUserSubscriptionsPosts(user, posts);

        ToDoubleFunction<Post> scoreFunction = p -> -Communities.postScore(p.points, time - p.sentTime);
        var expected = new ArrayList<>(posts);
        expected.sort(Comparator.comparingDouble(scoreFunction));
        expected.remove(expected.size() - 1);

        var result = users.getFeed(user);

        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    public void testRegister() {
        time = 10L;
        var id = 1;
        var name = "user";
        var email = Optional.of("user@mail");
        var password = "123";
        var hashedPassword = new Authenticator.HashedPassword(new byte[] { 1, 2, 3, 4 }, new byte[] { 5, 6 });
        var form = new UserRegisterForm(name, email, password);
        var user = new User(id, name, email, time, hashedPassword.hash, hashedPassword.salt, 0);
        when(validator.validateUserName(any())).thenReturn(true);
        when(validator.validateEmail(any())).thenReturn(true);
        when(authenticator.hashPassword(password)).thenReturn(hashedPassword);
        when(database.newUser(name, email, time, hashedPassword.hash, hashedPassword.salt)).thenReturn(user);

        var result = assertDoesNotThrow(() -> users.register(form));

        verify(database).newUser(name, email, time, hashedPassword.hash, hashedPassword.salt);
        verify(emailer).emailUser(user, "registered");
        assertEquals(user, result);
    }

    @Test
    public void testRegisterNameInvalid() {
        var form = new UserRegisterForm("user", Optional.of("user@mail"), "123");
        when(validator.validateUserName(any())).thenReturn(false);
        when(validator.validateEmail(any())).thenReturn(true);

        assertThrows(UserRegisterException.class, () -> users.register(form), "NameInvalid");
    }

    @Test
    public void testRegisterEmailInvalid() {
        var form = new UserRegisterForm("user", Optional.of("user@mail"), "123");
        when(validator.validateUserName(any())).thenReturn(true);
        when(validator.validateEmail(any())).thenReturn(false);

        assertThrows(UserRegisterException.class, () -> users.register(form), "EmailInvalid");
    }

    @Test
    public void testRegisterNameTaken() {
        var form = new UserRegisterForm("user", Optional.of("user@mail"), "123");
        when(validator.validateUserName(any())).thenReturn(true);
        when(validator.validateEmail(any())).thenReturn(true);
        when(database.getUserByName("user")).thenReturn(Optional.of(mockUser()));

        assertThrows(UserRegisterException.class, () -> users.register(form), "NameTaken");
    }

    @Test
    public void testAuthenticate() {
        var user = mockUser();
        var hashedPassword = new Authenticator.HashedPassword(user.passwordHash, user.passwordSalt);
        when(authenticator.authenticate(any(), eq(hashedPassword))).thenReturn(false);
        when(authenticator.authenticate("123", hashedPassword)).thenReturn(true);

        var result1 = users.authenticate(user, "xyz");
        var result2 = users.authenticate(user, "123");

        assertFalse(result1);
        assertTrue(result2);
    }

    @Test
    public void testSetEmail() {
        var user = mockUser();
        when(validator.validateEmail("new@mail")).thenReturn(true);

        var result = users.setEmail(user, Optional.of("new@mail"));

        verify(database).setUserEmail(user.id, Optional.of("new@mail"));
        verify(emailer).emailUser(user, "email-updated");
        assertTrue(result);
    }

    @Test
    public void testSetEmailEmpty() {
        var user = mockUser();

        var result = users.setEmail(user, Optional.empty());

        verify(database).setUserEmail(user.id, Optional.empty());
        verify(emailer).emailUser(user, "email-updated");
        assertTrue(result);
    }

    @Test
    public void testSetEmailInvalid() {
        var user = mockUser();
        when(validator.validateEmail("invalid")).thenReturn(false);

        var result = users.setEmail(user, Optional.of("invalid"));

        verify(database, never()).setUserEmail(anyLong(), any());
        verify(emailer, never()).emailUser(any(), any());
        assertFalse(result);
    }

    @Test
    public void testSetPassword() {
        var user = mockUser();
        var hashedPassword = new Authenticator.HashedPassword(new byte[] { 4, 3, 2, 1 }, new byte[] { 6, 5 });
        when(authenticator.hashPassword("xyz")).thenReturn(hashedPassword);

        users.setPassword(user, "xyz");

        verify(database).setUserPassword(user.id, hashedPassword.hash, hashedPassword.salt);
        verify(emailer).emailUser(user, "password-updated");
    }

    @Test
    public void testAddPoints() {
        var user = mockUser();

        users.addPoints(user, +1);

        verify(database).addUserPoints(user.id, +1);
    }

    @Test
    public void testDelete() {
        var user = mockUser();

        users.delete(user);

        verify(database).deleteUser(user.id);
        verify(emailer).emailUser(user, "deleted");
    }
}
