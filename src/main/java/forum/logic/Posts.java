package forum.logic;

import forum.entities.CommentTree;
import forum.services.Clock;
import forum.databases.PostsDatabase;
import forum.entities.Post;
import forum.entities.PostVote;
import forum.entities.User;
import forum.forms.PostSendForm;
import forum.services.Emailer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// logika biznesowa związana z postami
@AllArgsConstructor
public class Posts {
    private final PostsDatabase database;
    private final Clock clock;
    private final Emailer emailer;
    private final Users users;

    public Optional<Post> get(long id) {
        return database.getPost(id);
    }

    public Optional<PostVote> getVote(User voter, Post post) {
        return database.getPostVote(voter.id, post.id);
    }

    public List<CommentTree> getComments(Post post) {
        var comments = database.getPostComments(post.id);
        var topComments = comments.stream().filter(x -> x.parentId.isEmpty()).collect(Collectors.toList());
        var descendants = comments.stream().filter(x -> x.parentId.isPresent()).collect(Collectors.toList());
        return CommentTree.build(topComments, descendants);
    }

    public long countVotes(Post post) {
        return database.getPostVotes(post.id).size();
    }

    public Post send(User sender, PostSendForm form) {
        var time = clock.time();
        return database.newPost(sender.id, form.community.id, form.title, form.content, time);
    }

    public void edit(Post post, String content) {
        var time = clock.time();
        database.editPost(post.id, content, time);
    }

    private void vote(User voter, Post post, boolean upvote) {
        var previous = database.getPostVote(voter.id, post.id);
        var time = clock.time();

        if (previous.isEmpty()) {
            database.newPostVote(voter.id, post.id, upvote, time);
            database.addPostPoints(post.id, upvote ? +1 : -1);
            users.addPoints(post.sender, upvote ? +1 : -1);
        }

        else if (previous.get().upvote != upvote) {
            database.flipPostVote(voter.id, post.id, time);
            database.addPostPoints(post.id, upvote ? +2 : -2);
            users.addPoints(post.sender, upvote ? +2 : -2);
        }
    }

    public void upvote(User voter, Post post) {
        vote(voter, post, true);
    }

    public void downvote(User voter, Post post) {
        vote(voter, post, false);
    }

    public void unvote(PostVote vote) {
        database.deletePostVote(vote.voter.id, vote.post.id);
        database.addPostPoints(vote.post.id, vote.upvote ? -1 : +1);
        users.addPoints(vote.post.sender, vote.upvote ? -1 : +1);
    }

    public void delete(Post post, boolean byModerator) {
        if (byModerator)
            emailer.emailUser(post.sender, "post-deleted:" + post.id);
        database.deletePost(post.id);
    }
}
