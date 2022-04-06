package forum.databases;

import forum.entities.Community;
import forum.entities.Post;
import forum.entities.Subscriber;

import java.util.List;
import java.util.Optional;

// część interfejsu bazy danych związana ze społecznościami;
// jest ona wykorzystywana przez klasę forum.logic.Communities
public interface CommunitiesDatabase {
    Community newCommunity(String name, String description, long creatorId, long time);
    void newSubscriber(long userId, long communityId, long time);

    Optional<Community> getCommunity(long id);
    Optional<Community> getCommunityByName(String name);
    Optional<Subscriber> getSubscriber(long userId, long communityId);
    List<Community> getCommunities(long from, long to);
    List<Subscriber> getCommunitySubscribers(long id);
    List<Post> getCommunityPosts(long id, long newerThan, long olderThan);

    void setCommunityDescription(long id, String description);
    void addModerator(long userId, long communityId);
    void removeModerator(long userId, long communityId);

    void deleteCommunity(long id);
    void deleteSubscriber(long userId, long communityId);
}
