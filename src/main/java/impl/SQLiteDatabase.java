package impl;

import forum.databases.Database;
import forum.entities.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

// baza danych SQLite;
// zaletą tej implementacji jest jej prostota - baza danych jest zwykłym plikiem
public class SQLiteDatabase implements Database {
    private final Connection connection;

    private static final String file = "forum.db";

    public SQLiteDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + file);
            init();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // stworzenie tabel na dane, jeśli wcześniej nie istniały
    private void init() {
        update("create table if not exists users " +
                "(id integer primary key autoincrement, " +
                "name text not null unique, " +
                "email text default null, " +
                "joined_time integer not null, " +
                "password_hash blob not null, " +
                "password_salt blob not null, " +
                "points integer not null default 0)");

        update("create table if not exists communities " +
                "(id integer primary key autoincrement, " +
                "name text not null unique, " +
                "description text not null, " +
                "owner integer not null references users on delete cascade, " +
                "created_time integer not null)");

        update("create table if not exists posts " +
                "(id integer primary key autoincrement, " +
                "title text not null, " +
                "content text not null, " +
                "sender integer not null references users on delete cascade, " +
                "community integer not null references communities on delete cascade, " +
                "sent_time integer not null, " +
                "points integer not null default 0, " +
                "edited_time integer default null)");

        update("create table if not exists comments " +
                "(id integer primary key autoincrement, " +
                "content text not null, " +
                "sender integer not null references users on delete cascade, " +
                "post integer not null references posts on delete cascade, " +
                "parent integer references comments on delete cascade, " +
                "sent_time integer not null, " +
                "points integer not null default 0, " +
                "edited_time integer default null)");

        update("create table if not exists subscribers " +
                "(user integer not null references users on delete cascade, " +
                "community integer not null references communities on delete cascade, " +
                "subscribed_time integer not null, " +
                "moderator integer not null default 0, " +
                "primary key (user, community))");

        update("create table if not exists post_votes " +
                "(voter integer not null references users on delete cascade, " +
                "post integer not null references posts on delete cascade, " +
                "upvote integer not null, " +
                "vote_time integer not null, " +
                "primary key (voter, post))");

        update("create table if not exists comment_votes " +
                "(voter integer not null references users on delete cascade, " +
                "comment integer not null references comments on delete cascade, " +
                "upvote integer not null, " +
                "vote_time integer not null, " +
                "primary key (voter, comment))");
    }

    // metody konstruujące obiekty z pakietu forum.entities na podstawie rekordów w bazie danych

    private static User user(ResultSet result, int column) {
        try {
            return new User(result.getLong(column),
                    result.getString(column + 1),
                    Optional.ofNullable(result.getString(column + 2)),
                    result.getLong(column + 3),
                    result.getBytes(column + 4),
                    result.getBytes(column + 5),
                    result.getLong(column + 6));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Community community(ResultSet result, int column) {
        try {
            return new Community(result.getLong(column),
                    result.getString(column + 1),
                    result.getString(column + 2),
                    user(result, column + 5),
                    result.getLong(column + 4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Post post(ResultSet result, int column) {
        try {
            return new Post(result.getLong(column),
                    result.getString(column + 1),
                    result.getString(column + 2),
                    user(result, column + 8),
                    community(result, column + 15),
                    result.getLong(column + 5),
                    result.getLong(column + 6),
                    optionalLong(result, column + 7));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Comment comment(ResultSet result, int column) {
        try {
            return new Comment(result.getLong(column),
                    result.getString(column + 1),
                    user(result, column + 8),
                    post(result, column + 15),
                    optionalLong(result, column + 4),
                    result.getLong(column + 5),
                    result.getLong(column + 6),
                    optionalLong(result, column + 7));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static Subscriber subscriber(ResultSet result) {
        try {
            return new Subscriber(user(result, 1 + 4),
                    community(result, 1 + 11),
                    result.getLong(1 + 2),
                    result.getBoolean(1 + 3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static PostVote postVote(ResultSet result) {
        try {
            return new PostVote(user(result, 1 + 4),
                    post(result, 1 + 11),
                    result.getBoolean(1 + 2),
                    result.getLong(1 + 3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static CommentVote commentVote(ResultSet result) {
        try {
            return new CommentVote(user(result, 1 + 4),
                    comment(result, 1 + 11),
                    result.getBoolean(1 + 2),
                    result.getLong(1 + 3));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // często używane fragmenty SQL

    private static final String joinCommunity =
            "join users as owner on communities.owner = owner.id ";

    private static final String joinPost =
            "join users as post_sender on posts.sender = post_sender.id " +
            "join communities on posts.community = communities.id " + joinCommunity;

    private static final String joinComment =
            "join users as comment_sender on comments.sender = comment_sender.id " +
            "join posts on comments.post = posts.id " + joinPost;

    private static final String joinSubscriber =
            "join users as user on subscribers.user = user.id " +
            "join communities on subscribers.community = communities.id " + joinCommunity;

    private static final String joinPostVote =
            "join users as voter on post_votes.voter = voter.id " +
            "join posts on post_votes.post = posts.id " + joinPost;

    private static final String joinCommentVote =
            "join users as voter on comment_votes.voter = voter.id " +
            "join comments on comment_votes.comment = comments.id " + joinComment;

    @Override
    public User newUser(String name, Optional<String> email, long time, byte[] passwordHash, byte[] passwordSalt) {
        var id = insert(
                "insert into users " +
                "(name, email, joined_time, password_hash, password_salt) " +
                "values (?, ?, ?, ?, ?)",
                name, email, time, passwordHash, passwordSalt);
        var result = query("select * from users where id = ?", id);
        next(result);
        return user(result, 1);
    }

    @Override
    public Optional<User> getUser(long id) {
        var result = query("select * from users where id = ?", id);
        return next(result) ? Optional.of(user(result, 1)) : Optional.empty();
    }

    @Override
    public Optional<User> getUserByName(String name) {
        var result = query("select * from users where name = ?", name);
        return next(result) ? Optional.of(user(result, 1)) : Optional.empty();
    }

    @Override
    public List<Post> getUserPosts(long id) {
        var result = query(
                "select * from posts " + joinPost +
                "where posts.sender = ? " +
                "order by posts.sent_time desc",
                id);
        var list = new ArrayList<Post>();
        while (next(result))
            list.add(post(result, 1));
        return list;
    }

    @Override
    public List<Comment> getUserComments(long id) {
        var result = query(
                "select * from comments " + joinComment +
                "where comments.sender = ? " +
                "order by comments.sent_time desc",
                id);
        var list = new ArrayList<Comment>();
        while (next(result))
            list.add(comment(result, 1));
        return list;
    }

    @Override
    public List<Subscriber> getUserSubscriptions(long id) {
        var result = query(
                "select * from subscribers " + joinSubscriber +
                "where subscribers.user = ? " +
                "order by communities.name",
                id);
        var list = new ArrayList<Subscriber>();
        while (next(result))
            list.add(subscriber(result));
        return list;
    }

    @Override
    public List<PostVote> getUserPostVotes(long id) {
        var result = query(
                "select * from post_votes " + joinPostVote +
                "where post_votes.voter = ? " +
                "order by post_votes.vote_time desc",
                id);
        var list = new ArrayList<PostVote>();
        while (next(result))
            list.add(postVote(result));
        return list;
    }

    @Override
    public List<CommentVote> getUserCommentVotes(long id) {
        var result = query(
                "select * from comment_votes " + joinCommentVote +
                "where comment_votes.voter = ? " +
                "order by comment_votes.vote_time desc",
                id);
        var list = new ArrayList<CommentVote>();
        while (next(result))
            list.add(commentVote(result));
        return list;
    }

    @Override
    public List<Post> getUserSubscriptionsPosts(long id, long newerThan) {
        var result = query(
                "select * from posts " + joinPost +
                "join subscribers on posts.community = subscribers.community " +
                "where subscribers.user = ? and posts.sent_time > ? " +
                "order by posts.sent_time desc",
                id, newerThan);
        var list = new ArrayList<Post>();
        while (next(result))
            list.add(post(result, 1));
        return list;
    }

    @Override
    public void setUserEmail(long id, Optional<String> email) {
        update("update users " +
                "set email = ? " +
                "where id = ?",
                email, id);
    }

    @Override
    public void setUserPassword(long id, byte[] passwordHash, byte[] passwordSalt) {
        update("update users " +
                "set password_hash = ?, password_salt = ? " +
                "where id = ?",
                passwordHash, passwordSalt, id);
    }

    @Override
    public void addUserPoints(long id, long points) {
        update("update users " +
                "set points = points + ? " +
                "where id = ?",
                points, id);
    }

    @Override
    public void deleteUser(long id) {
        update("delete from users where id = ?", id);
    }

    @Override
    public Community newCommunity(String name, String description, long creatorId, long time) {
        var id = insert(
                "insert into communities " +
                "(name, description, owner, created_time) " +
                "values (?, ?, ?, ?)",
                name, description, creatorId, time);
        var result = query(
                "select * from communities " + joinCommunity +
                "where communities.id = ?",
                id);
        next(result);
        return community(result, 1);
    }

    @Override
    public void newSubscriber(long userId, long communityId, long time) {
        insert("insert into subscribers " +
                "(user, community, subscribed_time) " +
                "values (?, ?, ?)",
                userId, communityId, time);
    }

    @Override
    public Optional<Community> getCommunity(long id) {
        var result = query(
                "select * from communities " + joinCommunity +
                "where communities.id = ?",
                id);
        return next(result) ? Optional.of(community(result, 1)) : Optional.empty();
    }

    @Override
    public Optional<Community> getCommunityByName(String name) {
        var result = query(
                "select * from communities " + joinCommunity +
                "where communities.name = ?",
                name);
        return next(result) ? Optional.of(community(result, 1)) : Optional.empty();
    }

    @Override
    public Optional<Subscriber> getSubscriber(long userId, long communityId) {
        var result = query(
                "select * from subscribers " + joinSubscriber +
                "where subscribers.user = ? and subscribers.community = ?",
                userId, communityId);
        return next(result) ? Optional.of(subscriber(result)) : Optional.empty();
    }

    @Override
    public List<Community> getCommunities(long from, long to) {
        var result = query(
                "select * from communities " + joinCommunity +
                "order by communities.name " +
                "limit ? offset ?",
                to - from, from);
        var list = new ArrayList<Community>();
        while (next(result))
            list.add(community(result, 1));
        return list;
    }

    @Override
    public List<Subscriber> getCommunitySubscribers(long id) {
        var result = query(
                "select * from subscribers " + joinSubscriber +
                "where subscribers.community = ? " +
                "order by subscribers.subscribed_time desc",
                id);
        var list = new ArrayList<Subscriber>();
        while (next(result))
            list.add(subscriber(result));
        return list;
    }

    @Override
    public List<Post> getCommunityPosts(long id, long newerThan, long olderThan) {
        var result = query(
                "select * from posts " + joinPost +
                "where posts.community = ? and posts.sent_time > ? and posts.sent_time <= ? " +
                "order by posts.sent_time desc",
                id, newerThan, olderThan);
        var list = new ArrayList<Post>();
        while (next(result))
            list.add(post(result, 1));
        return list;
    }

    @Override
    public void setCommunityDescription(long id, String description) {
        update("update communities " +
                "set description = ? " +
                "where id = ?",
                description, id);
    }

    @Override
    public void addModerator(long userId, long communityId) {
        update("update subscribers " +
                "set moderator = 1 " +
                "where user = ? and community = ?",
                userId, communityId);
    }

    @Override
    public void removeModerator(long userId, long communityId) {
        update("update subscribers " +
                "set moderator = 0 " +
                "where user = ? and community = ?",
                userId, communityId);
    }

    @Override
    public void deleteCommunity(long id) {
        update("delete from communities where id = ?", id);
    }

    @Override
    public void deleteSubscriber(long userId, long communityId) {
        update("delete from subscribers " +
                "where user = ? and community = ?",
                userId, communityId);
    }

    @Override
    public Post newPost(long senderId, long communityId, String title, String content, long time) {
        var id = insert(
                "insert into posts " +
                "(sender, community, title, content, sent_time) " +
                "values (?, ?, ?, ?, ?)",
                senderId, communityId, title, content, time);
        var result = query(
                "select * from posts " + joinPost +
                "where posts.id = ?",
                id);
        next(result);
        return post(result, 1);
    }

    @Override
    public void newPostVote(long voterId, long postId, boolean upvote, long time) {
        insert("insert into post_votes " +
                "(voter, post, upvote, vote_time) " +
                "values (?, ?, ?, ?)",
                voterId, postId, upvote, time);
    }

    @Override
    public Optional<Post> getPost(long id) {
        var result = query(
                "select * from posts " + joinPost +
                "where posts.id = ?",
                id);
        return next(result) ? Optional.of(post(result, 1)) : Optional.empty();
    }

    @Override
    public Optional<PostVote> getPostVote(long voterId, long postId) {
        var result = query(
                "select * from post_votes " + joinPostVote +
                "where post_votes.voter = ? and post_votes.post = ?",
                voterId, postId);
        return next(result) ? Optional.of(postVote(result)) : Optional.empty();
    }

    @Override
    public List<Comment> getPostComments(long id) {
        var result = query(
                "select * from comments " + joinComment +
                "where comments.post = ? " +
                "order by comments.sent_time desc",
                id);
        var list = new ArrayList<Comment>();
        while (next(result))
            list.add(comment(result, 1));
        return list;
    }

    @Override
    public List<PostVote> getPostVotes(long id) {
        var result = query(
                "select * from post_votes " + joinPostVote +
                "where post_votes.post = ? " +
                "order by post_votes.vote_time desc",
                id);
        var list = new ArrayList<PostVote>();
        while (next(result))
            list.add(postVote(result));
        return list;
    }

    @Override
    public void editPost(long id, String content, long time) {
        update("update posts " +
                "set content = ?, edited_time = ? " +
                "where id = ?",
                content, time, id);
    }

    @Override
    public void addPostPoints(long id, long points) {
        update("update posts " +
                "set points = points + ? " +
                "where id = ?",
                points, id);
    }

    @Override
    public void flipPostVote(long voterId, long postId, long time) {
        update("update post_votes " +
                "set upvote = not upvote, vote_time = ? " +
                "where voter = ? and post = ?",
                time, voterId, postId);
    }

    @Override
    public void deletePost(long id) {
        update("delete from posts where id = ?", id);
    }

    @Override
    public void deletePostVote(long voterId, long postId) {
        update("delete from post_votes " +
                "where voter = ? and post = ?",
                voterId, postId);
    }

    @Override
    public Comment newComment(long senderId, long postId, OptionalLong parent, String content, long time) {
        var id = insert(
                "insert into comments " +
                "(sender, post, parent, content, sent_time) " +
                "values (?, ?, ?, ?, ?)",
                senderId, postId, parent, content, time);
        var result = query(
                "select * from comments " + joinComment +
                "where comments.id = ?",
                id);
        next(result);
        return comment(result, 1);
    }

    @Override
    public void newCommentVote(long voterId, long commentId, boolean upvote, long time) {
        insert("insert into comment_votes " +
                "(voter, comment, upvote, vote_time) " +
                "values (?, ?, ?, ?)",
                voterId, commentId, upvote, time);
    }

    @Override
    public Optional<Comment> getComment(long id) {
        var result = query(
                "select * from comments " + joinComment +
                "where comments.id = ?",
                id);
        return next(result) ? Optional.of(comment(result, 1)) : Optional.empty();
    }

    @Override
    public Optional<CommentVote> getCommentVote(long voterId, long commentId) {
        var result = query(
                "select * from comment_votes " + joinCommentVote +
                "where comment_votes.voter = ? and comment_votes.comment = ?",
                voterId, commentId);
        return next(result) ? Optional.of(commentVote(result)) : Optional.empty();
    }

    @Override
    public List<Comment> getCommentDescendants(long id) {
        var result = query(
                "select * from comments " + joinComment +
                "where comments.parent = ? " +
                "order by comments.sent_time desc",
                id);
        var list = new ArrayList<Comment>();
        while (next(result))
            list.add(comment(result, 1));
        var length = list.size();
        for (var i = 0; i < length; i++)
            list.addAll(getCommentDescendants(list.get(i).id));
        return list;
    }

    @Override
    public List<CommentVote> getCommentVotes(long id) {
        var result = query(
                "select * from comment_votes " + joinCommentVote +
                "where comment_votes.comment = ? " +
                "order by comments.sent_time desc",
                id);
        var list = new ArrayList<CommentVote>();
        while (next(result))
            list.add(commentVote(result));
        return list;
    }

    @Override
    public void editComment(long id, String content, long time) {
        update("update comments " +
                "set content = ?, edited_time = ? " +
                "where id = ?",
                content, time, id);
    }

    @Override
    public void addCommentPoints(long id, long points) {
        update("update comments " +
                "set points = points + ? " +
                "where id = ?",
                points, id);
    }

    @Override
    public void flipCommentVote(long voterId, long commentId, long time) {
        update("update comment_votes " +
                "set upvote = not upvote, vote_time = ? " +
                "where voter = ? and comment = ?",
                time, voterId, commentId);
    }

    @Override
    public void deleteComment(long id) {
        update("delete from comments where id = ?", id);
    }

    @Override
    public void deleteCommentVote(long voterId, long commentId) {
        update("delete from comment_votes " +
                "where voter = ? and comment = ?",
                voterId, commentId);
    }

    // pomocnicze metody wykonujące różne rodzaje zapytań SQL

    private ResultSet query(String sql, Object... arguments) {
        try {
            return statement(sql, arguments).executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long insert(String sql, Object... arguments) {
        try {
            var statement = statement(sql, arguments);
            statement.executeUpdate();
            return statement.getGeneratedKeys().getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void update(String sql, Object... arguments) {
        try {
            statement(sql, arguments).executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // pomocnicza metoda konstruująca zapytanie SQL z argumentami
    @SuppressWarnings("unchecked")
    private PreparedStatement statement(String sql, Object... arguments) throws SQLException {
        var statement = connection.prepareStatement(sql);

        for (var i = 1; i <= arguments.length; i++) {
            var arg = arguments[i - 1];
            if (arg instanceof String)
                statement.setString(i, (String) arg);
            else if (arg instanceof Long)
                statement.setLong(i, (Long) arg);
            else if (arg instanceof Boolean)
                statement.setBoolean(i, (Boolean) arg);
            else if (arg instanceof byte[])
                statement.setBytes(i, (byte[]) arg);
            else if (arg instanceof Optional) {
                var optional = (Optional<String>) arg;
                if (optional.isPresent())
                    statement.setString(i, optional.get());
            } else if (arg instanceof OptionalLong) {
                var optional = (OptionalLong) arg;
                if (optional.isPresent())
                    statement.setLong(i, optional.getAsLong());
            }
        }

        return statement;
    }

    // pomocnicze metody do przeglądania wyników zapytań SQL

    private static boolean next(ResultSet result) {
        try {
            return result.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static OptionalLong optionalLong(ResultSet result, int column) throws SQLException {
        var value = result.getLong(column);
        return result.wasNull() ? OptionalLong.empty() : OptionalLong.of(value);
    }
}
