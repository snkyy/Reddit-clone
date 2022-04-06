package forum.controllers;

import forum.logic.Forum;
import forum.controllers.annotations.*;
import forum.entities.CommentVote;
import forum.entities.PostVote;
import forum.forms.UserRegisterForm;
import forum.exceptions.UserRegisterException;
import forum.views.*;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

// kontrolery związane z użytkownikami
@AllArgsConstructor
@SuppressWarnings("unused")
public class UserControllers<R> {
    private final Forum forum;
    private final RequestContext request;
    private final Responses<R> responses;

    @Page
    @LoginOptional
    public R index() {
        var user = request.optionalLoggedInUser;
        var subscriptions = user.map(u -> forum.users().getSubscriptions(u)).orElse(List.of());
        var feed = user.map(u -> forum.users().getFeed(u)).orElse(List.of());
        return responses.view("index", new IndexView(user, subscriptions, feed));
    }

    @Page
    public R login() {
        var failed = request.parameters.containsKey("failed");
        return responses.view("login", new LoginView(failed));
    }

    @Action
    @ParameterRequired("name,password")
    public R doLogin() {
        var name = request.parameters.get("name");
        var password = request.parameters.get("password");

        var optional = forum.users().getByName(name);
        if (optional.isEmpty())
            return responses.redirect("login", "failed=true");

        var user = optional.get();
        if (!forum.users().authenticate(user, password))
            return responses.redirect("login", "failed=true");

        request.sessionStore.put("user", Long.toString(user.id));
        return responses.redirect("index");
    }

    @Page
    public R logout() {
        request.sessionStore.remove("user");
        return responses.redirect("index");
    }

    @Page
    public R register() {
        var nameInvalid = request.parameters.containsKey("name-invalid");
        var emailInvalid = request.parameters.containsKey("email-invalid");
        var nameTaken = request.parameters.containsKey("name-taken");
        var passwordsMismatch = request.parameters.containsKey("passwords-mismatch");
        return responses.view("register", new RegisterView(nameInvalid, emailInvalid, nameTaken, passwordsMismatch));
    }

    @Action
    @ParameterRequired("name,email,password,password-confirm")
    public R doRegister() {
        var name = request.parameters.get("name");
        var email = request.parameters.get("email");
        var password = request.parameters.get("password");
        var passwordConfirm = request.parameters.get("password-confirm");

        if (!password.equals(passwordConfirm))
            return responses.redirect("register", "passwords-mismatch=true");

        var form = new UserRegisterForm(name, email.isEmpty() ? Optional.empty() : Optional.of(email), password);

        try {
            var user = forum.users().register(form);
            request.sessionStore.put("user", Long.toString(user.id));
            return responses.redirect("index");
        }

        catch (UserRegisterException e) {
            switch (e.cause) {
                case NameInvalid:
                    return responses.redirect("register", "name-invalid=true");
                case EmailInvalid:
                    return responses.redirect("register", "email-invalid=true");
                case NameTaken:
                    return responses.redirect("register", "name-taken=true");
                default:
                    throw new RuntimeException(e);
            }
        }
    }

    @Page
    @LoginOptional
    @ParameterRequired("name")
    public R user() {
        var name = request.parameters.get("name");
        var optional = forum.users().getByName(name);
        if (optional.isEmpty())
            return responses.notFound("user-name:" + name);

        var user = optional.get();
        var self = request.optionalLoggedInUser.map(u -> u.equals(user)).orElse(false);
        var posts = forum.users().getPosts(user);
        var comments = forum.users().getComments(user);

        List<PostVote> postVotes;
        List<CommentVote> commentVotes;
        if (self) {
            postVotes = forum.users().getPostVotes(user);
            commentVotes = forum.users().getCommentVotes(user);
        } else {
            postVotes = List.of();
            commentVotes = List.of();
        }

        return responses.view("user", new UserView(user, self, posts, comments, postVotes, commentVotes));
    }

    @Page
    @LoginRequired
    public R account() {
        var user = request.loggedInUser;
        var emailInvalid = request.parameters.containsKey("email-invalid");
        var passwordsMismatch = request.parameters.containsKey("passwords-mismatch");
        return responses.view("account", new AccountView(user, emailInvalid, passwordsMismatch));
    }

    @Action
    @LoginRequired
    @ParameterRequired("email")
    public R updateEmail() {
        var user = request.loggedInUser;
        var email = request.parameters.get("email");

        if (!forum.users().setEmail(user, email.isEmpty() ? Optional.empty() : Optional.of(email)))
            return responses.redirect("account", "email-invalid=true");

        return responses.redirect("account");
    }

    @Action
    @LoginRequired
    @ParameterRequired("password,password-confirm")
    public R updatePassword() {
        var user = request.loggedInUser;
        var password = request.parameters.get("password");
        var passwordConfirm = request.parameters.get("password-confirm");

        if (!password.equals(passwordConfirm))
            return responses.redirect("account", "passwords-mismatch=true");

        forum.users().setPassword(user, password);
        return responses.redirect("account");
    }

    @Action
    @LoginRequired
    @ParameterRequired("password")
    public R deleteAccount() {
        var user = request.loggedInUser;
        var password = request.parameters.get("password");

        if (!forum.users().authenticate(user, password))
            return responses.redirect("account");

        forum.users().delete(user);
        return responses.redirect("index");
    }
}
