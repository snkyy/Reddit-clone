package forum.databases;

import forum.entities.*;

import java.util.List;
import java.util.Optional;

// część interfejsu bazy danych związana z użytkownikami;
// jest ona wykorzystywana przez klasę forum.logic.Users
public interface UsersDatabase {
    User newUser(String name, Optional<String> email, long time, byte[] passwordHash, byte[] passwordSalt);

    Optional<User> getUser(long id);
    Optional<User> getUserByName(String name);
    List<Post> getUserPosts(long id);
    List<Comment> getUserComments(long id);
    List<Subscriber> getUserSubscriptions(long id);
    List<PostVote> getUserPostVotes(long id);
    List<CommentVote> getUserCommentVotes(long id);
    List<Post> getUserSubscriptionsPosts(long id, long newerThan);

    void setUserEmail(long id, Optional<String> email);
    void setUserPassword(long id, byte[] passwordHash, byte[] passwordSalt);
    void addUserPoints(long id, long points);

    void deleteUser(long id);
}
