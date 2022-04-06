package forum.views;

import forum.entities.*;
import lombok.Value;

import java.util.List;

@Value
public class UserView {
    public User user;
    public boolean self;
    public List<Post> posts;
    public List<Comment> comments;
    public List<PostVote> postVotes;
    public List<CommentVote> commentVotes;
}
