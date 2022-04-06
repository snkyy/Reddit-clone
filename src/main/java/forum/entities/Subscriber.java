package forum.entities;

import lombok.Value;

// dane subskrybenta (tylko do odczytu)
@Value
public class Subscriber {
    public User user;
    public Community community;
    public long subscribedTime;
    public boolean moderator;
}
