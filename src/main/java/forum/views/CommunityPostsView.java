package forum.views;

import forum.entities.Community;
import forum.entities.Post;
import lombok.Value;

import java.util.List;

@Value
public class CommunityPostsView {
    public Community community;
    public long day;
    public List<Post> posts;
}
