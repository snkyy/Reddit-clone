package forum.entities;

import lombok.Value;

// dane głosu na post (tylko do odczytu)
@Value
public class PostVote {
    public User voter;
    public Post post;
    public boolean upvote;
    public long voteTime;
}
