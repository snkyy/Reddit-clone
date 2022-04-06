package forum.databases;

import forum.entities.Comment;
import forum.entities.Post;
import forum.entities.PostVote;

import java.util.List;
import java.util.Optional;

// część interfejsu bazy danych związana z postami;
// jest ona wykorzystywana przez klasę forum.logic.Posts
public interface PostsDatabase {
    Post newPost(long senderId, long communityId, String title, String content, long time);
    void newPostVote(long voterId, long postId, boolean upvote, long time);

    Optional<Post> getPost(long id);
    Optional<PostVote> getPostVote(long voterId, long postId);
    List<Comment> getPostComments(long id);
    List<PostVote> getPostVotes(long id);

    void editPost(long id, String content, long time);
    void addPostPoints(long id, long points);
    void flipPostVote(long voterId, long postId, long time);

    void deletePost(long id);
    void deletePostVote(long voterId, long postId);
}
