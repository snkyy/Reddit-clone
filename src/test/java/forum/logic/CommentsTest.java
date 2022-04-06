package forum.logic;

import forum.entities.Comment;
import forum.entities.CommentTree;
import forum.forms.CommentSendForm;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class CommentsTest extends LogicTestBase {
    private final Users users = mock(Users.class);
    private final Comments comments = new Comments(database, clock, emailer, users);

    @Test
    public void testGet() {
        var comment = mockComment();
        when(database.getComment(comment.id)).thenReturn(Optional.of(comment));

        var result1 = comments.get(comment.id);
        var result2 = comments.get(comment.id + 1);

        assertThat(result1).contains(comment);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetParent() {
        var post = mockPost();
        var parent = mockComment(post);
        var comment = mockComment(post, OptionalLong.of(parent.id));
        when(database.getComment(parent.id)).thenReturn(Optional.of(parent));

        var result1 = comments.getParent(comment);
        var result2 = comments.getParent(parent);

        assertThat(result1).contains(parent);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetVote() {
        var vote = mockCommentVote();
        when(database.getCommentVote(vote.voter.id, vote.comment.id)).thenReturn(Optional.of(vote));

        var result1 = comments.getVote(vote.voter, vote.comment);
        var result2 = comments.getVote(mockUser(), mockComment());

        assertThat(result1).contains(vote);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetTree() {
        var post = mockPost();
        var comment = mockComment(post);
        var child1 = mockComment(post, OptionalLong.of(comment.id));
        var child2 = mockComment(post, OptionalLong.of(comment.id));
        var child3 = mockComment(post, OptionalLong.of(child1.id));
        var descendants = List.of(child1, child2, child3);
        when(database.getCommentDescendants(comment.id)).thenReturn(descendants);

        var expected = new CommentTree(comment, new ArrayList<>());
        expected.children.add(new CommentTree(child1, new ArrayList<>()));
        expected.children.add(new CommentTree(child2, new ArrayList<>()));
        expected.children.get(0).children.add(new CommentTree(child3, new ArrayList<>()));

        var result = comments.getTree(comment);

        assertEquals(expected, result);
    }

    @Test
    public void testCountVotes() {
        var comment = mockComment();
        var vote1 = mockCommentVote(comment);
        var vote2 = mockCommentVote(comment);
        when(database.getCommentVotes(comment.id)).thenReturn(List.of(vote1, vote2));

        var result = comments.countVotes(comment);

        assertEquals(2, result);
    }

    @Test
    public void testSend() {
        time = 10;
        var sender = mockUser();
        var post = mockPost();
        var id = 1;
        var content = "content";
        var form = new CommentSendForm(post, content, Optional.empty());
        var comment = new Comment(id, content, sender, post, OptionalLong.empty(), time, 0, OptionalLong.empty());
        when(database.newComment(sender.id, post.id, OptionalLong.empty(), content, time)).thenReturn(comment);

        var result = comments.send(sender, form);

        verify(database).newComment(sender.id, post.id, OptionalLong.empty(), content, time);
        verify(emailer).emailUser(post.sender, "comment:" + post.id);
        assertEquals(comment, result);
    }

    @Test
    public void testSendReply() {
        time = 10;
        var sender = mockUser();
        var post = mockPost();
        var parent = mockComment(post);
        var id = parent.id + 1;
        var content = "content";
        var form = new CommentSendForm(post, content, Optional.of(parent));
        var comment = new Comment(id, content, sender, post, OptionalLong.of(parent.id), time, 0, OptionalLong.empty());
        when(database.newComment(sender.id, post.id, OptionalLong.of(parent.id), content, time)).thenReturn(comment);

        var result = comments.send(sender, form);

        verify(database).newComment(sender.id, post.id, OptionalLong.of(parent.id), content, time);
        verify(emailer).emailUser(parent.sender, "reply:" + parent.id);
        assertEquals(comment, result);
    }

    @Test
    public void testEdit() {
        time = 10;
        var comment = mockComment();

        comments.edit(comment, "test");

        verify(database).editComment(comment.id, "test", time);
    }

    @Test
    public void testUpvote() {
        time = 10;
        var voter = mockUser();
        var comment = mockComment();

        comments.upvote(voter, comment);

        verify(database).newCommentVote(voter.id, comment.id, true, time);
        verify(database).addCommentPoints(comment.id, +1);
        verify(users).addPoints(comment.sender, +1);
    }

    @Test
    public void testUpvoteAgain() {
        var vote = mockCommentVote(true);
        when(database.getCommentVote(vote.voter.id, vote.comment.id)).thenReturn(Optional.of(vote));

        comments.upvote(vote.voter, vote.comment);

        verify(database, never()).newCommentVote(anyLong(), anyLong(), anyBoolean(), anyLong());
        verify(database, never()).addCommentPoints(anyLong(), anyLong());
        verify(users, never()).addPoints(any(), anyLong());
    }

    @Test
    public void testUpvoteFlip() {
        var vote = mockCommentVote(false);
        when(database.getCommentVote(vote.voter.id, vote.comment.id)).thenReturn(Optional.of(vote));

        comments.upvote(vote.voter, vote.comment);

        verify(database).flipCommentVote(vote.voter.id, vote.comment.id, time);
        verify(database).addCommentPoints(vote.comment.id, +2);
        verify(users).addPoints(vote.comment.sender, +2);
    }

    @Test
    public void testDownvote() {
        time = 10;
        var voter = mockUser();
        var comment = mockComment();

        comments.downvote(voter, comment);

        verify(database).newCommentVote(voter.id, comment.id, false, time);
        verify(database).addCommentPoints(comment.id, -1);
        verify(users).addPoints(comment.sender, -1);
    }

    @Test
    public void testDownvoteAgain() {
        var vote = mockCommentVote(false);
        when(database.getCommentVote(vote.voter.id, vote.comment.id)).thenReturn(Optional.of(vote));

        comments.downvote(vote.voter, vote.comment);

        verify(database, never()).newCommentVote(anyLong(), anyLong(), anyBoolean(), anyLong());
        verify(database, never()).addCommentPoints(anyLong(), anyLong());
        verify(users, never()).addPoints(any(), anyLong());
    }

    @Test
    public void testDownvoteFlip() {
        var vote = mockCommentVote(true);
        when(database.getCommentVote(vote.voter.id, vote.comment.id)).thenReturn(Optional.of(vote));

        comments.downvote(vote.voter, vote.comment);

        verify(database).flipCommentVote(vote.voter.id, vote.comment.id, time);
        verify(database).addCommentPoints(vote.comment.id, -2);
        verify(users).addPoints(vote.comment.sender, -2);
    }

    @Test
    public void testUndoUpvote() {
        var vote = mockCommentVote(true);

        comments.unvote(vote);

        verify(database).deleteCommentVote(vote.voter.id, vote.comment.id);
        verify(database).addCommentPoints(vote.comment.id, -1);
        verify(users).addPoints(vote.comment.sender, -1);
    }

    @Test
    public void testUndoDownvote() {
        var vote = mockCommentVote(false);

        comments.unvote(vote);

        verify(database).deleteCommentVote(vote.voter.id, vote.comment.id);
        verify(database).addCommentPoints(vote.comment.id, +1);
        verify(users).addPoints(vote.comment.sender, +1);
    }

    @Test
    public void testDelete() {
        var comment = mockComment();

        comments.delete(comment, false);

        verify(database).deleteComment(comment.id);
    }

    @Test
    public void testDeleteByModerator() {
        var comment = mockComment();

        comments.delete(comment, true);

        verify(database).deleteComment(comment.id);
        verify(emailer).emailUser(comment.sender, "comment-deleted:" + comment.id);
    }
}
