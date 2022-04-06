package forum.logic;

import forum.entities.CommentTree;
import forum.entities.Post;
import forum.forms.PostSendForm;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PostsTest extends LogicTestBase {
    private final Users users = mock(Users.class);
    private final Posts posts = new Posts(database, clock, emailer, users);

    @Test
    public void testGet() {
        var post = mockPost();
        when(database.getPost(post.id)).thenReturn(Optional.of(post));

        var result1 = posts.get(post.id);
        var result2 = posts.get(post.id + 1);

        assertThat(result1).contains(post);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetVote() {
        var vote = mockPostVote();
        when(database.getPostVote(vote.voter.id, vote.post.id)).thenReturn(Optional.of(vote));

        var result1 = posts.getVote(vote.voter, vote.post);
        var result2 = posts.getVote(mockUser(), mockPost());

        assertThat(result1).contains(vote);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetComments() {
        var post = mockPost();
        var comment1 = mockComment(post);
        var comment2 = mockComment(post);
        var comment3 = mockComment(post, OptionalLong.of(comment1.id));
        var comment4 = mockComment(post, OptionalLong.of(comment2.id));
        var comment5 = mockComment(post, OptionalLong.of(comment2.id));
        var comments = List.of(comment1, comment2, comment3, comment4, comment5);

        var expected = new ArrayList<CommentTree>();
        expected.add(new CommentTree(comment1, new ArrayList<>()));
        expected.add(new CommentTree(comment2, new ArrayList<>()));
        expected.get(0).children.add(new CommentTree(comment3, new ArrayList<>()));
        expected.get(1).children.add(new CommentTree(comment4, new ArrayList<>()));
        expected.get(1).children.add(new CommentTree(comment5, new ArrayList<>()));

        when(database.getPostComments(post.id)).thenReturn(comments);

        var result = posts.getComments(post);

        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    public void testCountVotes() {
        var post = mockPost();
        var vote1 = mockPostVote(post);
        var vote2 = mockPostVote(post);
        when(database.getPostVotes(post.id)).thenReturn(List.of(vote1, vote2));

        var result = posts.countVotes(post);

        assertEquals(2, result);
    }

    @Test
    public void testSend() {
        time = 10;
        var id = 1;
        var sender = mockUser();
        var community = mockCommunity();
        var title = "title";
        var content = "content";
        var form = new PostSendForm(community, title, content);
        var post = new Post(id, title, content, sender, community, time, 0, OptionalLong.empty());
        when(database.newPost(sender.id, community.id, title, content, time)).thenReturn(post);

        var result = posts.send(sender, form);

        verify(database).newPost(sender.id, community.id, title, content, time);
        assertEquals(post, result);
    }

    @Test
    public void testEdit() {
        time = 10;
        var post = mockPost();

        posts.edit(post, "test");

        verify(database).editPost(post.id, "test", time);
    }

    @Test
    public void testUpvote() {
        time = 10;
        var voter = mockUser();
        var post = mockPost();

        posts.upvote(voter, post);

        verify(database).newPostVote(voter.id, post.id, true, time);
        verify(database).addPostPoints(post.id, +1);
        verify(users).addPoints(post.sender, +1);
    }

    @Test
    public void testUpvoteAgain() {
        var vote = mockPostVote(true);
        when(database.getPostVote(vote.voter.id, vote.post.id)).thenReturn(Optional.of(vote));

        posts.upvote(vote.voter, vote.post);

        verify(database, never()).newPostVote(anyLong(), anyLong(), anyBoolean(), anyLong());
        verify(database, never()).addPostPoints(anyLong(), anyLong());
        verify(users, never()).addPoints(any(), anyLong());
    }

    @Test
    public void testUpvoteFlip() {
        var vote = mockPostVote(false);
        when(database.getPostVote(vote.voter.id, vote.post.id)).thenReturn(Optional.of(vote));

        posts.upvote(vote.voter, vote.post);

        verify(database).flipPostVote(vote.voter.id, vote.post.id, time);
        verify(database).addPostPoints(vote.post.id, +2);
        verify(users).addPoints(vote.post.sender, +2);
    }

    @Test
    public void testDownvote() {
        time = 10;
        var voter = mockUser();
        var post = mockPost();

        posts.downvote(voter, post);

        verify(database).newPostVote(voter.id, post.id, false, time);
        verify(database).addPostPoints(post.id, -1);
        verify(users).addPoints(post.sender, -1);
    }

    @Test
    public void testDownvoteAgain() {
        var vote = mockPostVote(false);
        when(database.getPostVote(vote.voter.id, vote.post.id)).thenReturn(Optional.of(vote));

        posts.downvote(vote.voter, vote.post);

        verify(database, never()).newPostVote(anyLong(), anyLong(), anyBoolean(), anyLong());
        verify(database, never()).addPostPoints(anyLong(), anyLong());
        verify(users, never()).addPoints(any(), anyLong());
    }

    @Test
    public void testDownvoteFlip() {
        var vote = mockPostVote(true);
        when(database.getPostVote(vote.voter.id, vote.post.id)).thenReturn(Optional.of(vote));

        posts.downvote(vote.voter, vote.post);

        verify(database).flipPostVote(vote.voter.id, vote.post.id, time);
        verify(database).addPostPoints(vote.post.id, -2);
        verify(users).addPoints(vote.post.sender, -2);
    }

    @Test
    public void testUndoUpvote() {
        var vote = mockPostVote(true);

        posts.unvote(vote);

        verify(database).deletePostVote(vote.voter.id, vote.post.id);
        verify(database).addPostPoints(vote.post.id, -1);
        verify(users).addPoints(vote.post.sender, -1);
    }

    @Test
    public void testUndoDownvote() {
        var vote = mockPostVote(false);

        posts.unvote(vote);

        verify(database).deletePostVote(vote.voter.id, vote.post.id);
        verify(database).addPostPoints(vote.post.id, +1);
        verify(users).addPoints(vote.post.sender, +1);
    }

    @Test
    public void testDelete() {
        var post = mockPost();

        posts.delete(post, false);

        verify(database).deletePost(post.id);
    }

    @Test
    public void testDeleteByModerator() {
        var post = mockPost();

        posts.delete(post, true);

        verify(database).deletePost(post.id);
        verify(emailer).emailUser(post.sender, "post-deleted:" + post.id);
    }
}
