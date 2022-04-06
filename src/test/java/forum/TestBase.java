package forum;

import forum.entities.*;

import java.util.Optional;
import java.util.OptionalLong;

public abstract class TestBase {
    private long newUserId = 1;
    private long newCommunityId = 1;
    private long newPostId = 1;
    private long newCommentId = 1;

    protected User mockUser() {
        var id = newUserId++;
        var name = "user" + id;
        var email = Optional.of(name + "@mail");
        var joinedTime = 10L;
        var passwordHash = new byte[] { 1, 2, 3, 4 };
        var passwordSalt = new byte[] { 5, 6 };
        var points = 10L;
        return new User(id, name, email, joinedTime, passwordHash, passwordSalt, points);
    }

    protected Community mockCommunity(User creator) {
        var id = newCommunityId++;
        var name = "community" + id;
        var description = "description";
        var createdTime = 10L;
        return new Community(id, name, description, creator, createdTime);
    }

    protected Community mockCommunity() {
        return mockCommunity(mockUser());
    }

    protected Post mockPost(User sender, Community community, long sentTime, long points) {
        var id = newPostId++;
        var title = "post" + id;
        var content = "content";
        var editedTime = OptionalLong.empty();
        return new Post(id, title, content, sender, community, sentTime, points, editedTime);
    }

    protected Post mockPost(Community community, long sentTime, long points) {
        return mockPost(mockUser(), community, sentTime, points);
    }

    protected Post mockPost(long sentTime, long points) {
        return mockPost(mockUser(), mockCommunity(), sentTime, points);
    }

    protected Post mockPost(User sender, Community community) {
        return mockPost(sender, community, 10, 10);
    }

    protected Post mockPost(User sender) {
        return mockPost(sender, mockCommunity());
    }

    protected Post mockPost(Community community) {
        return mockPost(mockUser(), community);
    }

    protected Post mockPost() {
        return mockPost(mockUser(), mockCommunity());
    }

    protected Comment mockComment(User sender, Post post, OptionalLong parentId) {
        var id = newCommentId++;
        var content = "content";
        var sentTime = 10L;
        var points = 10L;
        var editedTime = OptionalLong.empty();
        return new Comment(id, content, sender, post, parentId, sentTime, points, editedTime);
    }

    protected Comment mockComment(User sender) {
        return mockComment(sender, mockPost(), OptionalLong.empty());
    }

    protected Comment mockComment(Post post, OptionalLong parentId) {
        return mockComment(mockUser(), post, parentId);
    }

    protected Comment mockComment(Post post) {
        return mockComment(mockUser(), post, OptionalLong.empty());
    }

    protected Comment mockComment() {
        return mockComment(mockUser(), mockPost(), OptionalLong.empty());
    }

    protected Subscriber mockSubscriber(User user, Community community, boolean moderator) {
        var subscribedTime = 10L;
        return new Subscriber(user, community, subscribedTime, moderator);
    }

    protected Subscriber mockSubscriber(User user) {
        return mockSubscriber(user, mockCommunity(), false);
    }

    protected Subscriber mockSubscriber(User user, Community community) {
        return mockSubscriber(user, community, false);
    }

    protected Subscriber mockSubscriber(Community community, boolean moderator) {
        return mockSubscriber(mockUser(), community, moderator);
    }

    protected Subscriber mockSubscriber(Community community) {
        return mockSubscriber(mockUser(), community, false);
    }

    protected Subscriber mockSubscriber(boolean moderator) {
        return mockSubscriber(mockUser(), mockCommunity(), moderator);
    }

    protected Subscriber mockSubscriber() {
        return mockSubscriber(mockUser(), mockCommunity(), false);
    }

    protected PostVote mockPostVote(User voter, Post post, boolean upvote) {
        var voteTime = 10L;
        return new PostVote(voter, post, upvote, voteTime);
    }

    protected PostVote mockPostVote(User voter, Post post) {
        return mockPostVote(voter, post, true);
    }

    protected PostVote mockPostVote(User user) {
        return mockPostVote(user, mockPost(), true);
    }

    protected PostVote mockPostVote(Post post) {
        return mockPostVote(mockUser(), post, true);
    }

    protected PostVote mockPostVote(boolean upvote) {
        return mockPostVote(mockUser(), mockPost(), upvote);
    }

    protected PostVote mockPostVote() {
        return mockPostVote(mockUser(), mockPost(), true);
    }

    protected CommentVote mockCommentVote(User voter, Comment comment, boolean upvote) {
        var voteTime = 10L;
        return new CommentVote(voter, comment, upvote, voteTime);
    }

    protected CommentVote mockCommentVote(User voter, Comment comment) {
        return mockCommentVote(voter, comment, true);
    }

    protected CommentVote mockCommentVote(User user) {
        return mockCommentVote(user, mockComment(), true);
    }

    protected CommentVote mockCommentVote(Comment comment) {
        return mockCommentVote(mockUser(), comment, true);
    }

    protected CommentVote mockCommentVote(boolean upvote) {
        return mockCommentVote(mockUser(), mockComment(), upvote);
    }

    protected CommentVote mockCommentVote() {
        return mockCommentVote(mockUser(), mockComment(), true);
    }
}