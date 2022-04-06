package forum.logic;

import forum.services.Clock;
import forum.databases.CommentsDatabase;
import forum.entities.*;
import forum.forms.CommentSendForm;
import forum.services.Emailer;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

// logika biznesowa związana z komentarzami
@AllArgsConstructor
public class Comments {
    private final CommentsDatabase database;
    private final Clock clock;
    private final Emailer emailer;
    private final Users users;

    public Optional<Comment> get(long id) {
        return database.getComment(id);
    }

    public Optional<Comment> getParent(Comment comment) {
        return comment.parentId.stream().boxed().flatMap(x -> database.getComment(x).stream()).findAny();
    }

    public Optional<CommentVote> getVote(User voter, Comment comment) {
        return database.getCommentVote(voter.id, comment.id);
    }

    public CommentTree getTree(Comment comment) {
        var descendants = database.getCommentDescendants(comment.id);
        return CommentTree.build(List.of(comment), descendants).get(0);
    }

    public long countVotes(Comment comment) {
        return database.getCommentVotes(comment.id).size();
    }

    public Comment send(User sender, CommentSendForm form) {
        var time = clock.time();
        var parentId = OptionalLong.empty();

        if (form.parent.isPresent()) {
            var parent = form.parent.get();
            emailer.emailUser(parent.sender, "reply:" + parent.id);
            parentId = OptionalLong.of(parent.id);
        } else
            emailer.emailUser(form.post.sender, "comment:" + form.post.id);

        return database.newComment(sender.id, form.post.id, parentId, form.content, time);
    }

    public void edit(Comment comment, String content) {
        var time = clock.time();
        database.editComment(comment.id, content, time);
    }

    private void vote(User voter, Comment comment, boolean upvote) {
        var previous = database.getCommentVote(voter.id, comment.id);
        var time = clock.time();

        if (previous.isEmpty()) {
            database.newCommentVote(voter.id, comment.id, upvote, time);
            database.addCommentPoints(comment.id, upvote ? +1 : -1);
            users.addPoints(comment.sender, upvote ? +1 : -1);
        }

        else if (previous.get().upvote != upvote) {
            database.flipCommentVote(voter.id, comment.id, time);
            database.addCommentPoints(comment.id, upvote ? +2 : -2);
            users.addPoints(comment.sender, upvote ? +2 : -2);
        }
    }

    public void upvote(User voter, Comment comment) {
        vote(voter, comment, true);
    }

    public void downvote(User voter, Comment comment) {
        vote(voter, comment, false);
    }

    public void unvote(CommentVote vote) {
        database.deleteCommentVote(vote.voter.id, vote.comment.id);
        database.addCommentPoints(vote.comment.id, vote.upvote ? -1 : +1);
        users.addPoints(vote.comment.sender, vote.upvote ? -1 : +1);
    }

    public void delete(Comment comment, boolean byModerator) {
        if (byModerator)
            emailer.emailUser(comment.sender, "comment-deleted:" + comment.id);
        database.deleteComment(comment.id);
    }
}
