package forum.views;

import forum.entities.Comment;
import forum.entities.User;
import lombok.Value;

@Value
public class SendReplyView {
    public User loggedInUser;
    public Comment comment;
}
