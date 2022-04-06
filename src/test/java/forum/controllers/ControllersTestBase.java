package forum.controllers;

import forum.*;
import forum.entities.Comment;
import forum.entities.Community;
import forum.entities.Post;
import forum.entities.User;
import forum.logic.*;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

public abstract class ControllersTestBase extends TestBase {
    protected final Map<String, String> requestParameters = new HashMap<>();
    protected final Map<String, String> sessionStore = new HashMap<>();
    protected final RequestContext request = new RequestContext(requestParameters, sessionStore);
    protected final Forum forum = mock(Forum.class);
    protected final Users users = mock(Users.class);
    protected final Communities communities = mock(Communities.class);
    protected final Posts posts = mock(Posts.class);
    protected final Comments comments = mock(Comments.class);
    protected final Responses<Object> responses = new MockResponses();

    protected ControllersTestBase() {
        when(forum.users()).thenReturn(users);
        when(forum.communities()).thenReturn(communities);
        when(forum.posts()).thenReturn(posts);
        when(forum.comments()).thenReturn(comments);
    }

    protected User login(User user) {
        sessionStore.put("user", Long.toString(user.id));
        when(users.get(user.id)).thenReturn(Optional.of(user));
        return user;
    }

    protected Community setup(Community community) {
        requestParameters.put("community", Long.toString(community.id));
        when(communities.get(community.id)).thenReturn(Optional.of(community));
        return community;
    }

    protected Post setup(Post post) {
        requestParameters.put("post", Long.toString(post.id));
        when(posts.get(post.id)).thenReturn(Optional.of(post));
        return post;
    }

    protected Comment setup(Comment comment) {
        requestParameters.put("comment", Long.toString(comment.id));
        when(comments.get(comment.id)).thenReturn(Optional.of(comment));
        return comment;
    }

    @Value
    protected static class View {
        public String template;
        public Object data;
    }

    @Value
    protected static class Redirect {
        public String path;
        public String[] parameters;

        public Redirect(String path, String... parameters) {
            this.path = path;
            this.parameters = parameters;
        }
    }

    @Value
    protected static class NotFound {
        public String what;
    }

    @Value
    protected static class BadRequest {
        public String message;
    }

    @Value
    protected static class Forbidden {
        public String message;
    }

    private static class MockResponses implements Responses<Object> {
        @Override
        public Object view(String template, Object data) {
            return new View(template, data);
        }

        @Override
        public Object redirect(String controllerName, String... parameters) {
            return new Redirect(controllerName, parameters);
        }

        @Override
        public Object notFound(String what) {
            return new NotFound(what);
        }

        @Override
        public Object badRequest(String message) {
            return new BadRequest(message);
        }

        @Override
        public Object forbidden(String message) {
            return new Forbidden(message);
        }
    }
}
