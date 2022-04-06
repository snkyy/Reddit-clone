package forum.views;

import forum.entities.Community;
import forum.entities.Post;
import forum.entities.Subscriber;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class CommunityView {
    public Community community;
    public List<Post> popularPosts;
    public long subscribers;
    public Optional<Subscriber> loggedInSubscriber;
}
