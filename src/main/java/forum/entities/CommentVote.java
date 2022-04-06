package forum.entities;

import lombok.Value;

// dane głosu na komentarz (tylko do odczytu)
@Value
public class CommentVote {
    public User voter;
    public Comment comment;
    public boolean upvote;
    public long voteTime;
}
