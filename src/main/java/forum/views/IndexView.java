package forum.views;

import forum.entities.Post;
import forum.entities.Subscriber;
import forum.entities.User;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class IndexView {
    public Optional<User> loggedInUser;
    public List<Subscriber> subscriptions;
    public List<Post> feed;
}
