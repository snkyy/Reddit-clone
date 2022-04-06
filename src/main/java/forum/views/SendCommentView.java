package forum.views;

import forum.entities.Post;
import forum.entities.User;
import lombok.Value;

@Value
public class SendCommentView {
    public User loggedInUser;
    public Post post;
}
