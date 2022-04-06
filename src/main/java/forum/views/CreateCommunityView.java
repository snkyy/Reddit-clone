package forum.views;

import forum.entities.User;
import lombok.Value;

@Value
public class CreateCommunityView {
    public User loggedInUser;
    public boolean nameInvalid;
    public boolean nameTaken;
}
