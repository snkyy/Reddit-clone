package forum.controllers;

import forum.entities.CommentTree;
import forum.forms.PostSendForm;
import forum.views.PostView;
import forum.views.SendPostView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostControllersTest extends ControllersTestBase {
    private Controller<Object> controller(String methodName) {
        return ControllerMethod.build(PostControllers.class, methodName);
    }

    @Test
    public void testPost() {
        var controller = controller("post");
        var post = setup(mockPost());
        var comments = List.of(new CommentTree(mockComment(post), List.of()));
        when(posts.getComments(post)).thenReturn(comments);
        when(posts.countVotes(post)).thenReturn(10L);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("post", new PostView(post, comments, 10L, false, false, false, Optional.empty())), result);
    }

    @Test
    public void testPostVoted() {
        var controller = controller("post");
        var user = login(mockUser());
        var post = setup(mockPost());
        var vote = mockPostVote(user, post);
        var comments = List.of(new CommentTree(mockComment(post), List.of()));
        when(posts.getComments(post)).thenReturn(comments);
        when(posts.countVotes(post)).thenReturn(10L);
        when(posts.getVote(user, post)).thenReturn(Optional.of(vote));

        var result = controller.control(forum, request, responses);

        assertEquals(new View("post", new PostView(post, comments, 10L, true, false, false, Optional.of(vote))), result);
    }

    @Test
    public void testPostSender() {
        var controller = controller("post");
        var user = login(mockUser());
        var post = setup(mockPost(user));
        var comments = List.of(new CommentTree(mockComment(post), List.of()));
        when(posts.getComments(post)).thenReturn(comments);
        when(posts.countVotes(post)).thenReturn(10L);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("post", new PostView(post, comments, 10L, true, true, false, Optional.empty())), result);
    }

    @Test
    public void testPostModerator() {
        var controller = controller("post");
        var user = login(mockUser());
        var community = mockCommunity();
        var subscriber = mockSubscriber(user, community, true);
        var post = setup(mockPost(community));
        var comments = List.of(new CommentTree(mockComment(post), List.of()));
        when(posts.getComments(post)).thenReturn(comments);
        when(posts.countVotes(post)).thenReturn(10L);
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(subscriber));

        var result = controller.control(forum, request, responses);

        assertEquals(new View("post", new PostView(post, comments, 10L, true, false, true, Optional.empty())), result);
    }

    @Test
    public void testPostNotFound() {
        var controller = controller("post");
        requestParameters.put("post", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("post:1"), result);
    }

    @Test
    public void testSendPost() {
        var controller = controller("sendPost");
        var user = login(mockUser());
        var community = setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        assertEquals(new View("send-post", new SendPostView(user, community)), result);
    }

    @Test
    public void testSendPostNotLoggedIn() {
        var controller = controller("sendPost");
        setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testSendPostCommunityNotFound() {
        var controller = controller("sendPost");
        login(mockUser());
        requestParameters.put("community", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("community:1"), result);
    }

    @Test
    public void testDoSendPost() {
        var controller = controller("doSendPost");
        var user = login(mockUser());
        var community = setup(mockCommunity());
        var title = "title";
        var content = "content";
        var form = new PostSendForm(community, title, content);
        requestParameters.put("title", title);
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(posts).send(user, form);
        assertEquals(new Redirect("community", "name=" + community.name), result);
    }

    @Test
    public void testEditPost() {
        var controller = controller("editPost");
        var user = login(mockUser());
        var post = setup(mockPost(user));
        var content = "test";
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(posts).edit(post, content);
        assertEquals(new Redirect("post", "post=" + post.id),  result);
    }

    @Test
    public void testEditPostNotSender() {
        var controller = controller("editPost");
        login(mockUser());
        var post = setup(mockPost());
        var content = "test";
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(posts, never()).edit(any(), any());
        assertEquals(new Forbidden("not-post-sender:" + post.id), result);
    }

    @Test
    public void testDeletePost() {
        var controller = controller("deletePost");
        var user = login(mockUser());
        var post = setup(mockPost(user));
        requestParameters.put("confirm", "true");

        var result = controller.control(forum, request, responses);

        verify(posts).delete(post, false);
        assertEquals(new Redirect("community", "name=" + post.community.name),  result);
    }

    @Test
    public void testDeletePostModerator() {
        var controller = controller("deletePost");
        var user = login(mockUser());
        var community = mockCommunity();
        var post = setup(mockPost(community));
        var moderator = mockSubscriber(user, community, true);
        requestParameters.put("confirm", "true");
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(moderator));

        var result = controller.control(forum, request, responses);

        verify(posts).delete(post, true);
        assertEquals(new Redirect("community", "name=" + post.community.name),  result);
    }

    @Test
    public void testDeletePostNoConfirm() {
        var controller = controller("deletePost");
        var user = login(mockUser());
        var post = setup(mockPost(user));

        var result = controller.control(forum, request, responses);

        verify(posts, never()).delete(any(), anyBoolean());
        assertEquals(new Redirect("post", "post=" + post.id),  result);
    }

    @Test
    public void testDeletePostNotSender() {
        var controller = controller("deletePost");
        login(mockUser());
        var post = setup(mockPost());
        requestParameters.put("confirm", "true");

        var result = controller.control(forum, request, responses);

        verify(posts, never()).delete(any(), anyBoolean());
        assertEquals(new Forbidden("not-post-sender:" + post.id), result);
    }

    @Test
    public void testUpvotePost() {
        var controller = controller("upvotePost");
        var user = login(mockUser());
        var post = setup(mockPost());

        var result = controller.control(forum, request, responses);

        verify(posts).upvote(user, post);
        assertEquals(new Redirect("post", "post=" + post.id),  result);
    }

    @Test
    public void testDownvotePost() {
        var controller = controller("downvotePost");
        var user = login(mockUser());
        var post = setup(mockPost());

        var result = controller.control(forum, request, responses);

        verify(posts).downvote(user, post);
        assertEquals(new Redirect("post", "post=" + post.id),  result);
    }

    @Test
    public void testUnvotePost() {
        var controller = controller("unvotePost");
        var user = login(mockUser());
        var post = setup(mockPost());
        var vote = mockPostVote(user, post);
        when(posts.getVote(user, post)).thenReturn(Optional.of(vote));

        var result = controller.control(forum, request, responses);

        verify(posts).unvote(vote);
        assertEquals(new Redirect("post", "post=" + post.id),  result);
    }
}
