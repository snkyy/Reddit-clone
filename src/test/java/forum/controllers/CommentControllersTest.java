package forum.controllers;

import forum.entities.CommentTree;
import forum.forms.CommentSendForm;
import forum.views.CommentView;
import forum.views.SendCommentView;
import forum.views.SendReplyView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommentControllersTest extends ControllersTestBase {
    private Controller<Object> controller(String methodName) {
        return ControllerMethod.build(CommentControllers.class, methodName);
    }

    @Test
    public void testComment() {
        var controller = controller("comment");
        var post = mockPost();
        var parent = mockComment(post);
        var comment = setup(mockComment(post, OptionalLong.of(parent.id)));
        var tree = new CommentTree(comment, List.of(new CommentTree(mockComment(comment.post), List.of())));
        when(comments.getTree(comment)).thenReturn(tree);
        when(comments.getParent(comment)).thenReturn(Optional.of(parent));
        when(comments.countVotes(comment)).thenReturn(10L);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("comment", new CommentView(tree, Optional.of(parent), 10L, false, false, false, Optional.empty())), result);
    }

    @Test
    public void testCommentVoted() {
        var controller = controller("comment");
        var user = login(mockUser());
        var comment = setup(mockComment());
        var vote = mockCommentVote(user, comment);
        var tree = new CommentTree(comment, List.of(new CommentTree(mockComment(comment.post), List.of())));
        when(comments.getTree(comment)).thenReturn(tree);
        when(comments.countVotes(comment)).thenReturn(10L);
        when(comments.getVote(user, comment)).thenReturn(Optional.of(vote));

        var result = controller.control(forum, request, responses);

        assertEquals(new View("comment", new CommentView(tree, Optional.empty(), 10L, true, false, false, Optional.of(vote))), result);
    }

    @Test
    public void testCommentSender() {
        var controller = controller("comment");
        var user = login(mockUser());
        var comment = setup(mockComment(user));
        var tree = new CommentTree(comment, List.of(new CommentTree(mockComment(comment.post), List.of())));
        when(comments.getTree(comment)).thenReturn(tree);
        when(comments.countVotes(comment)).thenReturn(10L);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("comment", new CommentView(tree, Optional.empty(), 10L, true, true, false, Optional.empty())), result);
    }

    @Test
    public void testCommentModerator() {
        var controller = controller("comment");
        var user = login(mockUser());
        var community = mockCommunity();
        var subscriber = mockSubscriber(user, community, true);
        var post = mockPost(community);
        var comment = setup(mockComment(post));
        var tree = new CommentTree(comment, List.of(new CommentTree(mockComment(comment.post), List.of())));
        when(comments.getTree(comment)).thenReturn(tree);
        when(comments.countVotes(comment)).thenReturn(10L);
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(subscriber));

        var result = controller.control(forum, request, responses);

        assertEquals(new View("comment", new CommentView(tree, Optional.empty(), 10L, true, false, true, Optional.empty())), result);
    }

    @Test
    public void testCommentNotFound() {
        var controller = controller("comment");
        requestParameters.put("comment", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("comment:1"), result);
    }

    @Test
    public void testSendComment() {
        var controller = controller("sendComment");
        var user = login(mockUser());
        var post = setup(mockPost());

        var result = controller.control(forum, request, responses);

        assertEquals(new View("send-comment", new SendCommentView(user, post)), result);
    }

    @Test
    public void testSendCommentNotLoggedIn() {
        var controller = controller("sendComment");
        setup(mockPost());

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testSendCommentPostNotFound() {
        var controller = controller("sendComment");
        login(mockUser());
        requestParameters.put("post", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("post:1"), result);
    }

    @Test
    public void testSendReply() {
        var controller = controller("sendReply");
        var user = login(mockUser());
        var comment = setup(mockComment());

        var result = controller.control(forum, request, responses);

        assertEquals(new View("send-reply", new SendReplyView(user, comment)), result);
    }

    @Test
    public void testSendReplyNotLoggedIn() {
        var controller = controller("sendReply");
        setup(mockComment());

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testSendReplyPostNotFound() {
        var controller = controller("sendReply");
        login(mockUser());
        requestParameters.put("comment", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("comment:1"), result);
    }

    @Test
    public void testDoSendComment() {
        var controller = controller("doSendComment");
        var user = login(mockUser());
        var post = setup(mockPost());
        var content = "content";
        var form = new CommentSendForm(post, content, Optional.empty());
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(comments).send(user, form);
        assertEquals(new Redirect("post", "post=" + post.id), result);
    }

    @Test
    public void testDoSendReply() {
        var controller = controller("doSendReply");
        var user = login(mockUser());
        var post = mockPost();
        var parent = setup(mockComment(post));
        var content = "content";
        var form = new CommentSendForm(post, content, Optional.of(parent));
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(comments).send(user, form);
        assertEquals(new Redirect("post", "post=" + post.id), result);
    }

    @Test
    public void testEditComment() {
        var controller = controller("editComment");
        var user = login(mockUser());
        var comment = setup(mockComment(user));
        var content = "test";
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(comments).edit(comment, content);
        assertEquals(new Redirect("comment", "comment=" + comment.id),  result);
    }

    @Test
    public void testEditCommentNotSender() {
        var controller = controller("editComment");
        login(mockUser());
        var comment = setup(mockComment());
        var content = "test";
        requestParameters.put("content", content);

        var result = controller.control(forum, request, responses);

        verify(comments, never()).edit(any(), any());
        assertEquals(new Forbidden("not-comment-sender:" + comment.id), result);
    }

    @Test
    public void testDeleteComment() {
        var controller = controller("deleteComment");
        var user = login(mockUser());
        var comment = setup(mockComment(user));
        requestParameters.put("confirm", "true");

        var result = controller.control(forum, request, responses);

        verify(comments).delete(comment, false);
        assertEquals(new Redirect("post", "post=" + comment.post.id),  result);
    }

    @Test
    public void testDeleteCommentModerator() {
        var controller = controller("deleteComment");
        var user = login(mockUser());
        var community = mockCommunity();
        var post = mockPost(community);
        var comment = setup(mockComment(post));
        var moderator = mockSubscriber(user, community, true);
        requestParameters.put("confirm", "true");
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(moderator));

        var result = controller.control(forum, request, responses);

        verify(comments).delete(comment, true);
        assertEquals(new Redirect("post", "post=" + comment.post.id),  result);
    }

    @Test
    public void testDeleteCommentNoConfirm() {
        var controller = controller("deleteComment");
        var user = login(mockUser());
        var comment = setup(mockComment(user));

        var result = controller.control(forum, request, responses);

        verify(comments, never()).delete(any(), anyBoolean());
        assertEquals(new Redirect("comment", "comment=" + comment.id),  result);
    }

    @Test
    public void testDeleteCommentNotSender() {
        var controller = controller("deleteComment");
        login(mockUser());
        var comment = setup(mockComment());
        requestParameters.put("confirm", "true");

        var result = controller.control(forum, request, responses);

        verify(comments, never()).delete(any(), anyBoolean());
        assertEquals(new Forbidden("not-comment-sender:" + comment.id), result);
    }

    @Test
    public void testUpvoteComment() {
        var controller = controller("upvoteComment");
        var user = login(mockUser());
        var comment = setup(mockComment());

        var result = controller.control(forum, request, responses);

        verify(comments).upvote(user, comment);
        assertEquals(new Redirect("comment", "comment=" + comment.id),  result);
    }

    @Test
    public void testDownvoteComment() {
        var controller = controller("downvoteComment");
        var user = login(mockUser());
        var comment = setup(mockComment());

        var result = controller.control(forum, request, responses);

        verify(comments).downvote(user, comment);
        assertEquals(new Redirect("comment", "comment=" + comment.id),  result);
    }

    @Test
    public void testUnvoteComment() {
        var controller = controller("unvoteComment");
        var user = login(mockUser());
        var comment = setup(mockComment());
        var vote = mockCommentVote(user, comment);
        when(comments.getVote(user, comment)).thenReturn(Optional.of(vote));

        var result = controller.control(forum, request, responses);

        verify(comments).unvote(vote);
        assertEquals(new Redirect("comment", "comment=" + comment.id),  result);
    }
}
