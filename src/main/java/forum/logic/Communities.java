package forum.logic;

import forum.exceptions.CommunityCreateException;
import forum.validators.CommunityValidator;
import forum.services.Clock;
import forum.services.Emailer;
import forum.databases.CommunitiesDatabase;
import forum.entities.Community;
import forum.entities.Post;
import forum.entities.Subscriber;
import forum.entities.User;
import forum.forms.CommunityCreateForm;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static forum.exceptions.CommunityCreateException.Cause.*;

// logika biznesowa związana ze społecznościami
@AllArgsConstructor
public class Communities {
    private final CommunitiesDatabase database;
    private final Clock clock;
    private final CommunityValidator validator;
    private final Emailer emailer;

    // wzór na "popularność" postu na podstawie liczby puntków i czasu wysłania;
    // w praktyce oznacza on, że liczba punktów dzieli się przez 10 po upływie jednej doby
    public static double postScore(long points, long ageSeconds) {
        return 24 * 3600 * Math.log10(Math.max(1, points)) - ageSeconds;
    }

    public static final long communityListPageSize = 10;
    public static final long popularPostsMaxAgeDays = 7;
    public static final long popularPostsMaxCount = 100;

    public Optional<Community> get(long id) {
        return database.getCommunity(id);
    }

    public Optional<Community> getByName(String name) {
        return database.getCommunityByName(name);
    }

    public Optional<Subscriber> getSubscriber(User user, Community community) {
        return database.getSubscriber(user.id, community.id);
    }

    public List<Community> getCommunities(long page) {
        var from = page * communityListPageSize;
        var to = (page + 1) * communityListPageSize;
        return database.getCommunities(from, to);
    }

    public List<Post> getPosts(Community community, long day) {
        var time = clock.time();
        var newerThan = time - 24 * 3600 * (day + 1);
        var olderThan = time - 24 * 3600 * day;
        return database.getCommunityPosts(community.id, newerThan, olderThan);
    }

    public List<Post> getPopularPosts(Community community) {
        var time = clock.time();
        var newerThan = time - 24 * 3600 * popularPostsMaxAgeDays;
        var posts = database.getCommunityPosts(community.id, newerThan, time);

        ToDoubleFunction<Post> scoreFunction = p -> -postScore(p.points, time - p.sentTime);
        posts = new ArrayList<>(posts);
        posts.sort(Comparator.comparingDouble(scoreFunction));

        return posts.stream().limit(popularPostsMaxCount).collect(Collectors.toList());
    }

    public Community create(User creator, CommunityCreateForm form) throws CommunityCreateException {
        if (!validator.validateCommunityName(form.name))
            throw new CommunityCreateException(NameInvalid);
        if (database.getCommunityByName(form.name).isPresent())
            throw new CommunityCreateException(NameTaken);

        var time = clock.time();
        var community = database.newCommunity(form.name, form.description, creator.id, time);

        database.newSubscriber(creator.id, community.id, time);
        database.addModerator(creator.id, community.id);
        emailer.emailAdmin("community-created:" + community.name);
        emailer.emailUser(creator, "community-created:" + community.name);

        return community;
    }

    public void subscribe(User user, Community community) {
        var time = clock.time();
        if (database.getSubscriber(user.id, community.id).isEmpty())
            database.newSubscriber(user.id, community.id, time);
    }

    public void unsubscribe(Subscriber subscriber) {
        if (subscriber.user.equals(subscriber.community.owner))
            return;
        database.deleteSubscriber(subscriber.user.id, subscriber.community.id);
    }

    public long countSubscribers(Community community) {
        return database.getCommunitySubscribers(community.id).size();
    }

    public List<Subscriber> getModerators(Community community) {
        var subscriber = database.getCommunitySubscribers(community.id);
        return subscriber.stream().filter(s -> s.moderator).collect(Collectors.toList());
    }

    public void setDescription(Community community, String description) {
        database.setCommunityDescription(community.id, description);
    }

    public void addModerator(Subscriber subscriber) {
        if (subscriber.moderator)
            return;
        database.addModerator(subscriber.user.id, subscriber.community.id);
        emailer.emailUser(subscriber.user, "moderator-added:" + subscriber.community.name);
    }

    public void removeModerator(Subscriber subscriber) {
        if (!subscriber.moderator)
            return;
        if (subscriber.user.equals(subscriber.community.owner))
            return;
        database.removeModerator(subscriber.user.id, subscriber.community.id);
        emailer.emailUser(subscriber.user, "moderator-removed:" + subscriber.community.name);
    }

    public void delete(Community community) {
        database.deleteCommunity(community.id);
        emailer.emailAdmin("community-deleted:" + community.name);
        emailer.emailUser(community.owner, "community-deleted:" + community.name);
    }
}
