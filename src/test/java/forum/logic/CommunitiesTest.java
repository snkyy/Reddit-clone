package forum.logic;

import forum.entities.Community;
import forum.entities.Post;
import forum.forms.CommunityCreateForm;
import forum.exceptions.CommunityCreateException;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommunitiesTest extends LogicTestBase {
    private final Communities communities = new Communities(database, clock, validator, emailer);

    @Test
    public void testGet() {
        var community = mockCommunity();
        when(database.getCommunity(community.id)).thenReturn(Optional.of(community));

        var result1 = communities.get(community.id);
        var result2 = communities.get(community.id + 1);

        assertThat(result1).hasValue(community);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetByName() {
        var community = mockCommunity();
        when(database.getCommunityByName(community.name)).thenReturn(Optional.of(community));

        var result1 = communities.getByName(community.name);
        var result2 = communities.getByName(community.name + "1");

        assertThat(result1).hasValue(community);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetSubscriber() {
        var subscriber = mockSubscriber();
        when(database.getSubscriber(subscriber.user.id, subscriber.community.id)).thenReturn(Optional.of(subscriber));

        var result1 = communities.getSubscriber(subscriber.user, subscriber.community);
        var result2 = communities.getSubscriber(mockUser(), mockCommunity());

        assertThat(result1).hasValue(subscriber);
        assertThat(result2).isEmpty();
    }

    @Test
    public void testGetCommunities() {
        var pageSize = Communities.communityListPageSize;
        var list1 = new ArrayList<Community>();
        var list2 = new ArrayList<Community>();
        for (var i = 0; i < pageSize; i++) {
            list1.add(mockCommunity());
            list2.add(mockCommunity());
        }

        when(database.getCommunities(0, pageSize)).thenReturn(list1);
        when(database.getCommunities(pageSize, 2 * pageSize)).thenReturn(list2);

        var result1 = communities.getCommunities(0);
        var result2 = communities.getCommunities(1);

        assertThat(result1).containsExactlyElementsOf(list1);
        assertThat(result2).containsExactlyElementsOf(list2);
    }

    private void implGetCommunityPosts(Community community, List<Post> posts) {
        Answer<List<Post>> answer = inv -> {
            var newerThan = (long) inv.getArgument(1);
            var olderThan = (long) inv.getArgument(2);
            Predicate<Post> predicate = p -> p.sentTime > newerThan && p.sentTime <= olderThan;
            return posts.stream().filter(predicate).collect(Collectors.toList());
        };

        when(database.getCommunityPosts(eq(community.id), anyLong(), anyLong())).then(answer);
    }

    @Test
    public void testGetPosts() {
        time = 48 * 3600;
        var community = mockCommunity();
        var post1 = mockPost(community, 10 * 3600, 0);
        var post2 = mockPost(community, 20 * 3600, 0);
        var post3 = mockPost(community, 30 * 3600, 0);
        var post4 = mockPost(community, 40 * 3600, 0);
        var posts = List.of(post1, post2, post3, post4);
        implGetCommunityPosts(community, posts);

        var result1 = communities.getPosts(community, 0);
        var result2 = communities.getPosts(community, 1);

        assertThat(result1).containsExactly(post3, post4);
        assertThat(result2).containsExactly(post1, post2);
    }

    @Test
    public void testGetPopularPosts() {
        time = 24 * 3600 * Communities.popularPostsMaxAgeDays;
        var community = mockCommunity();
        var minPoints = -100;
        var maxPoints = 10000;

        var posts = new ArrayList<Post>();
        var random = new Random(0);
        for (var i = 0; i < Communities.popularPostsMaxCount + 1; i++) {
            var sentTime = 1 + random.nextInt((int)time);
            var points = minPoints + random.nextInt(maxPoints - minPoints);
            posts.add(mockPost(community, sentTime, points));
        }

        implGetCommunityPosts(community, posts);

        ToDoubleFunction<Post> scoreFunction = p -> -Communities.postScore(p.points, time - p.sentTime);
        var expected = new ArrayList<>(posts);
        expected.sort(Comparator.comparingDouble(scoreFunction));
        expected.remove(expected.size() - 1);

        var result = communities.getPopularPosts(community);

        assertThat(result).containsExactlyElementsOf(expected);
    }

    @Test
    public void testCreate() {
        time = 10L;
        var id = 1;
        var name = "community";
        var description = "description";
        var creator = mockUser();
        var form = new CommunityCreateForm(name, description);
        var community = new Community(id, name, description, creator, time);
        when(validator.validateCommunityName(any())).thenReturn(true);
        when(database.newCommunity(name, description, creator.id, time)).thenReturn(community);

        var result = assertDoesNotThrow(() -> communities.create(creator, form));

        verify(database).newCommunity(name, description, creator.id, time);
        verify(database).newSubscriber(creator.id, community.id, time);
        verify(database).addModerator(creator.id, community.id);
        verify(emailer).emailAdmin("community-created:community");
        verify(emailer).emailUser(creator, "community-created:community");
        assertEquals(community, result);
    }

    @Test
    public void testCreateNameInvalid() {
        var creator = mockUser();
        var form = new CommunityCreateForm("community", "description");
        when(validator.validateCommunityName(any())).thenReturn(false);

        assertThrows(CommunityCreateException.class, () -> communities.create(creator, form), "NameInvalid");
    }

    @Test
    public void testCreateNameTaken() {
        var creator = mockUser();
        var form = new CommunityCreateForm("community", "description");
        when(validator.validateCommunityName(any())).thenReturn(true);
        when(database.getCommunityByName("community")).thenReturn(Optional.of(mockCommunity()));

        assertThrows(CommunityCreateException.class, () -> communities.create(creator, form), "NameTaken");
    }

    @Test
    public void testSubscribe() {
        time = 10;
        var user = mockUser();
        var community = mockCommunity();

        communities.subscribe(user, community);

        verify(database).newSubscriber(user.id, community.id, time);
    }

    @Test
    public void testSubscribeAgain() {
        var user = mockUser();
        var community = mockCommunity();
        when(database.getSubscriber(user.id, community.id)).thenReturn(Optional.of(mockSubscriber(user, community)));

        communities.subscribe(user, community);

        verify(database, never()).newSubscriber(anyLong(), anyLong(), anyLong());
    }

    @Test
    public void testUnsubscribe() {
        var subscriber = mockSubscriber();

        communities.unsubscribe(subscriber);

        verify(database).deleteSubscriber(subscriber.user.id, subscriber.community.id);
    }

    @Test
    public void testUnsubscribeCreator() {
        var creator = mockUser();
        var community = mockCommunity(creator);
        var subscriber = mockSubscriber(creator, community);

        communities.unsubscribe(subscriber);

        verify(database, never()).deleteSubscriber(anyLong(), anyLong());
    }

    @Test
    public void testCountSubscribers() {
        var community = mockCommunity();
        var subscriber1 = mockSubscriber(community);
        var subscriber2 = mockSubscriber(community);
        when(database.getCommunitySubscribers(community.id)).thenReturn(List.of(subscriber1, subscriber2));

        var result = communities.countSubscribers(community);

        assertEquals(2, result);
    }

    @Test
    public void testGetModerators() {
        var community = mockCommunity();
        var subscriber1 = mockSubscriber(community, false);
        var subscriber2 = mockSubscriber(community, true);
        when(database.getCommunitySubscribers(community.id)).thenReturn(List.of(subscriber1, subscriber2));

        var result = communities.getModerators(community);

        assertThat(result).containsExactly(subscriber2);
    }

    @Test
    public void testSetDescription() {
        var community = mockCommunity();

        communities.setDescription(community, "test");

        verify(database).setCommunityDescription(community.id, "test");
    }

    @Test
    public void testAddModerator() {
        var subscriber = mockSubscriber(false);

        communities.addModerator(subscriber);

        verify(database).addModerator(subscriber.user.id, subscriber.community.id);
        verify(emailer).emailUser(subscriber.user, "moderator-added:" + subscriber.community.name);
    }

    @Test
    public void testAddModeratorAgain() {
        var subscriber = mockSubscriber(true);

        communities.addModerator(subscriber);

        verify(database, never()).addModerator(anyLong(), anyLong());
        verify(emailer, never()).emailUser(any(), any());
    }

    @Test
    public void testRemoveModerator() {
        var subscriber = mockSubscriber(true);

        communities.removeModerator(subscriber);

        verify(database).removeModerator(subscriber.user.id, subscriber.community.id);
        verify(emailer).emailUser(subscriber.user, "moderator-removed:" + subscriber.community.name);
    }

    @Test
    public void testRemoveModeratorAgain() {
        var subscriber = mockSubscriber(false);

        communities.removeModerator(subscriber);

        verify(database, never()).addModerator(anyLong(), anyLong());
        verify(emailer, never()).emailUser(any(), any());
    }

    @Test
    public void testRemoveModeratorCreator() {
        var creator = mockUser();
        var community = mockCommunity(creator);
        var subscriber = mockSubscriber(creator, community, true);

        communities.removeModerator(subscriber);

        verify(database, never()).addModerator(anyLong(), anyLong());
        verify(emailer, never()).emailUser(any(), any());
    }

    @Test
    public void testDelete() {
        var community = mockCommunity();

        communities.delete(community);

        verify(database).deleteCommunity(community.id);
        verify(emailer).emailAdmin("community-deleted:" + community.name);
        verify(emailer).emailUser(community.owner, "community-deleted:" + community.name);
    }
}
