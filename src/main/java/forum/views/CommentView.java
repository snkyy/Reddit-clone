package forum.views;

import forum.entities.Comment;
import forum.entities.CommentTree;
import forum.entities.CommentVote;
import lombok.Value;

import java.util.Optional;

@Value
public class CommentView {
    public CommentTree commentTree;
    public Optional<Comment> parent;
    public long votes;
    public boolean loggedIn;
    public boolean sender;
    public boolean moderator;
    public Optional<CommentVote> vote;
}
