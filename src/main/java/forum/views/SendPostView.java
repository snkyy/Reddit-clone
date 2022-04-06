package forum.views;

import forum.entities.Community;
import forum.entities.User;
import lombok.Value;

@Value
public class SendPostView {
    public User loggedInUser;
    public Community community;
}
