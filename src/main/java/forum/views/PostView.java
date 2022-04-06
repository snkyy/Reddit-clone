package forum.views;

import forum.entities.CommentTree;
import forum.entities.Post;
import forum.entities.PostVote;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class PostView {
    public Post post;
    public List<CommentTree> comments;
    public long votes;
    public boolean loggedIn;
    public boolean sender;
    public boolean moderator;
    public Optional<PostVote> vote;
}
