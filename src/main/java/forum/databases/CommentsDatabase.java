package forum.databases;

import forum.entities.*;

import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

// część interfejsu bazy danych związana z komentarzami;
// jest ona wykorzystywana przez klasę forum.logic.Comments
public interface CommentsDatabase {
    Comment newComment(long senderId, long postId, OptionalLong parent, String content, long time);
    void newCommentVote(long voterId, long commentId, boolean upvote, long time);

    Optional<Comment> getComment(long id);
    Optional<CommentVote> getCommentVote(long voterId, long commentId);
    List<Comment> getCommentDescendants(long id);
    List<CommentVote> getCommentVotes(long id);

    void editComment(long id, String content, long time);
    void addCommentPoints(long id, long points);
    void flipCommentVote(long voterId, long commentId, long time);

    void deleteComment(long id);
    void deleteCommentVote(long voterId, long commentId);
}
