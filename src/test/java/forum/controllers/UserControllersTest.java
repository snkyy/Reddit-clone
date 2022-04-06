package forum.controllers;

import forum.entities.User;
import forum.forms.UserRegisterForm;
import forum.exceptions.UserRegisterException;
import forum.views.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static forum.exceptions.UserRegisterException.Cause.NameTaken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllersTest extends ControllersTestBase {
    private Controller<Object> controller(String methodName) {
        return ControllerMethod.build(UserControllers.class, methodName);
    }

    @Test
    public void testIndex() {
        var controller = controller("index");
        var user = login(mockUser());
        var subscription1 = mockSubscriber(user);
        var subscription2 = mockSubscriber(user);
        var subscriptions = List.of(subscription1, subscription2);
        var feed = List.of(mockPost(), mockPost());
        when(users.getSubscriptions(user)).thenReturn(subscriptions);
        when(users.getFeed(user)).thenReturn(feed);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("index", new IndexView(Optional.of(user), subscriptions, feed)), result);
    }

    @Test
    public void testIndexNotLoggedIn() {
        var controller = controller("index");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("index", new IndexView(Optional.empty(), List.of(), List.of())), result);
    }

    @Test
    public void testLogin() {
        var controller = controller("login");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("login", new LoginView(false)), result);
    }

    @Test
    public void testLoginFailed() {
        var controller = controller("login");
        requestParameters.put("failed", "true");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("login", new LoginView(true)), result);
    }

    @Test
    public void testDoLogin() {
        var controller = controller("doLogin");
        var user = mockUser();
        var password = "123";
        requestParameters.put("name", user.name);
        requestParameters.put("password", password);
        when(users.getByName(user.name)).thenReturn(Optional.of(user));
        when(users.authenticate(user, password)).thenReturn(true);

        var result = controller.control(forum, request, responses);

        assertEquals(Long.toString(user.id), sessionStore.get("user"));
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testDoLoginInvalidName() {
        var controller = controller("doLogin");
        requestParameters.put("name", "test");
        requestParameters.put("password", "123");

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login", "failed=true"), result);
    }

    @Test
    public void testDoLoginInvalidPassword() {
        var controller = controller("doLogin");
        var user = mockUser();
        requestParameters.put("name", user.name);
        requestParameters.put("password", "123");
        when(users.getByName(user.name)).thenReturn(Optional.of(user));
        when(users.authenticate(eq(user), any())).thenReturn(false);

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login", "failed=true"), result);
    }

    @Test
    public void testLogout() {
        var controller = controller("logout");
        login(mockUser());

        var result = controller.control(forum, request, responses);
        assertFalse(sessionStore.containsKey("user"));
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testLogoutAgain() {
        var controller = controller("logout");

        var result = controller.control(forum, request, responses);
        assertFalse(sessionStore.containsKey("user"));
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testRegister() {
        var controller = controller("register");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("register", new RegisterView(false, false, false, false)), result);
    }

    @Test
    public void testRegisterNameTaken() {
        var controller = controller("register");
        requestParameters.put("name-taken", "true");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("register", new RegisterView(false, false, true, false)), result);
    }

    @Test
    public void testRegisterPasswordsMismatch() {
        var controller = controller("register");
        requestParameters.put("passwords-mismatch", "true");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("register", new RegisterView(false, false, false, true)), result);
    }

    @Test
    public void testDoRegister() throws UserRegisterException {
        var controller = controller("doRegister");
        var name = "user";
        var email = "user@mail";
        var password = "123";
        var form = new UserRegisterForm(name, Optional.of(email), password);
        var user = new User(1, name, Optional.of(email), 0, new byte[0], new byte[0], 0);
        requestParameters.put("name", name);
        requestParameters.put("email", email);
        requestParameters.put("password", password);
        requestParameters.put("password-confirm", password);
        when(users.register(form)).thenReturn(user);

        var result = controller.control(forum, request, responses);

        verify(users).register(form);
        assertEquals(Long.toString(user.id), sessionStore.get("user"));
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testDoRegisterNoEmail() throws UserRegisterException {
        var controller = controller("doRegister");
        var name = "user";
        var password = "123";
        var form = new UserRegisterForm(name, Optional.empty(), password);
        var user = new User(1, name, Optional.empty(), 0, new byte[0], new byte[0], 0);
        requestParameters.put("name", name);
        requestParameters.put("email", "");
        requestParameters.put("password", password);
        requestParameters.put("password-confirm", password);
        when(users.register(form)).thenReturn(user);

        var result = controller.control(forum, request, responses);

        verify(users).register(form);
        assertEquals(Long.toString(user.id), sessionStore.get("user"));
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testDoRegisterPasswordsMismatch() {
        var controller = controller("doRegister");
        requestParameters.put("name", "user");
        requestParameters.put("email", "user@mail");
        requestParameters.put("password", "123");
        requestParameters.put("password-confirm", "124");

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("register", "passwords-mismatch=true"), result);
    }

    @Test
    public void testDoRegisterNameTaken() throws UserRegisterException {
        var controller = controller("doRegister");
        var user = mockUser();
        var email = "user@mail";
        var password = "123";
        var form = new UserRegisterForm(user.name, Optional.of(email), password);
        requestParameters.put("name", user.name);
        requestParameters.put("email", email);
        requestParameters.put("password", password);
        requestParameters.put("password-confirm", password);
        when(users.register(form)).thenThrow(new UserRegisterException(NameTaken));

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("register", "name-taken=true"), result);
    }

    @Test
    public void testUser() {
        var controller = controller("user");
        var user = mockUser();
        var posts = List.of(mockPost(user), mockPost(user));
        var comments = List.of(mockComment(user), mockComment(user));
        var postVotes = List.of(mockPostVote(user), mockPostVote(user));
        var commentVotes = List.of(mockCommentVote(user), mockCommentVote(user));
        requestParameters.put("name", user.name);
        when(users.getByName(user.name)).thenReturn(Optional.of(user));
        when(users.getPosts(user)).thenReturn(posts);
        when(users.getComments(user)).thenReturn(comments);
        when(users.getPostVotes(user)).thenReturn(postVotes);
        when(users.getCommentVotes(user)).thenReturn(commentVotes);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("user", new UserView(user, false, posts, comments, List.of(), List.of())), result);
    }

    @Test
    public void testUserSelf() {
        var controller = controller("user");
        var user = login(mockUser());
        var posts = List.of(mockPost(user), mockPost(user));
        var comments = List.of(mockComment(user), mockComment(user));
        var postVotes = List.of(mockPostVote(user), mockPostVote(user));
        var commentVotes = List.of(mockCommentVote(user), mockCommentVote(user));
        requestParameters.put("name", user.name);
        when(users.getByName(user.name)).thenReturn(Optional.of(user));
        when(users.getPosts(user)).thenReturn(posts);
        when(users.getComments(user)).thenReturn(comments);
        when(users.getPostVotes(user)).thenReturn(postVotes);
        when(users.getCommentVotes(user)).thenReturn(commentVotes);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("user", new UserView(user, true, posts, comments, postVotes, commentVotes)), result);
    }

    @Test
    public void testUserNotFound() {
        var controller = controller("user");
        requestParameters.put("name", "test");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("user-name:test"), result);
    }

    @Test
    public void testAccount() {
        var controller = controller("account");
        var user = login(mockUser());

        var result = controller.control(forum, request, responses);

        assertEquals(new View("account", new AccountView(user, false, false)), result);
    }

    @Test
    public void testAccountPasswordsMismatch() {
        var controller = controller("account");
        var user = login(mockUser());
        requestParameters.put("passwords-mismatch", "true");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("account", new AccountView(user, false, true)), result);
    }

    @Test
    public void testAccountNotLoggedIn() {
        var controller = controller("account");

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testUpdateEmail() {
        var controller = controller("updateEmail");
        var user = login(mockUser());
        var email = "test@mail";
        requestParameters.put("email", email);
        when(users.setEmail(user, Optional.of(email))).thenReturn(true);

        var result = controller.control(forum, request, responses);

        verify(users).setEmail(user, Optional.of(email));
        assertEquals(new Redirect("account"), result);
    }

    @Test
    public void testUpdateEmailEmpty() {
        var controller = controller("updateEmail");
        var user = login(mockUser());
        requestParameters.put("email", "");
        when(users.setEmail(user, Optional.empty())).thenReturn(true);

        var result = controller.control(forum, request, responses);

        verify(users).setEmail(user, Optional.empty());
        assertEquals(new Redirect("account"), result);
    }

    @Test
    public void testUpdateEmailInvalid() {
        var controller = controller("updateEmail");
        var user = login(mockUser());
        var email = "invalid";
        requestParameters.put("email", email);
        when(users.setEmail(user, Optional.of(email))).thenReturn(false);

        var result = controller.control(forum, request, responses);

        verify(users).setEmail(user, Optional.of(email));
        assertEquals(new Redirect("account", "email-invalid=true"), result);
    }

    @Test
    public void testUpdatePassword() {
        var controller = controller("updatePassword");
        var user = login(mockUser());
        var password = "123";
        requestParameters.put("password", password);
        requestParameters.put("password-confirm", password);

        var result = controller.control(forum, request, responses);

        verify(users).setPassword(user, password);
        assertEquals(new Redirect("account"), result);
    }

    @Test
    public void testUpdatePasswordMismatch() {
        var controller = controller("updatePassword");
        login(mockUser());
        requestParameters.put("password", "123");
        requestParameters.put("password-confirm", "124");

        var result = controller.control(forum, request, responses);

        verify(users, never()).setPassword(any(), any());
        assertEquals(new Redirect("account", "passwords-mismatch=true"), result);
    }

    @Test
    public void testDeleteAccount() {
        var controller = controller("deleteAccount");
        var user = login(mockUser());
        var password = "123";
        requestParameters.put("password", password);
        when(users.authenticate(user, password)).thenReturn(true);

        var result = controller.control(forum, request, responses);

        verify(users).delete(user);
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testDeleteAccountFailed() {
        var controller = controller("deleteAccount");
        var user = login(mockUser());
        requestParameters.put("password", "123");
        when(users.authenticate(eq(user), any())).thenReturn(false);

        var result = controller.control(forum, request, responses);

        verify(users, never()).delete(any());
        assertEquals(new Redirect("account"), result);
    }
}
