package forum.views;

import forum.entities.Community;
import forum.entities.Subscriber;
import lombok.Value;

import java.util.List;

@Value
public class ManageCommunityView {
    public Community community;
    public List<Subscriber> moderators;
    public boolean subscriberNotFound;
}
