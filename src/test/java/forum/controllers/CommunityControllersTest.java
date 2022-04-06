package forum.controllers;

import forum.entities.Community;
import forum.forms.CommunityCreateForm;
import forum.exceptions.CommunityCreateException;
import forum.views.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static forum.exceptions.CommunityCreateException.Cause.NameTaken;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommunityControllersTest extends ControllersTestBase {
    private Controller<Object> controller(String methodName) {
        return ControllerMethod.build(CommunityControllers.class, methodName);
    }

    @Test
    public void testCommunityList() {
        var controller = controller("communityList");
        var community1 = mockCommunity();
        var community2 = mockCommunity();
        var list = List.of(community1, community2);
        requestParameters.put("page", "1");
        when(communities.getCommunities(1)).thenReturn(list);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("community-list", new CommunityListView(1, list)), result);
    }

    @Test
    public void testCreateCommunity() {
        var controller = controller("createCommunity");
        var user = login(mockUser());

        var result = controller.control(forum, request, responses);

        assertEquals(new View("create-community", new CreateCommunityView(user, false, false)), result);
    }

    @Test
    public void testCreateCommunityNameTaken() {
        var controller = controller("createCommunity");
        var user = login(mockUser());
        requestParameters.put("name-taken", "true");

        var result = controller.control(forum, request, responses);

        assertEquals(new View("create-community", new CreateCommunityView(user, false, true)), result);
    }

    @Test
    public void testCreateCommunityNotLoggedIn() {
        var controller = controller("createCommunity");

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testDoCreateCommunity() throws CommunityCreateException {
        var controller = controller("doCreateCommunity");
        var creator = login(mockUser());
        var name = "community";
        var description = "description";
        var form = new CommunityCreateForm(name, description);
        var community = new Community(1, name, description, creator, 0);
        requestParameters.put("name", name);
        requestParameters.put("description", description);
        when(communities.create(creator, form)).thenReturn(community);

        var result = controller.control(forum, request, responses);

        verify(communities).create(creator, form);
        assertEquals(new Redirect("community", "name=" + name), result);
    }

    @Test
    public void testDoCreateCommunityNameTaken() throws CommunityCreateException {
        var controller = controller("doCreateCommunity");
        var creator = login(mockUser());
        var name = "community";
        var description = "description";
        var form = new CommunityCreateForm(name, description);
        requestParameters.put("name", name);
        requestParameters.put("description", description);
        when(communities.create(creator, form)).thenThrow(new CommunityCreateException(NameTaken));

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("create-community", "name-taken=true"), result);
    }

    @Test
    public void testDoCreateCommunityNotLoggedIn() {
        var controller = controller("doCreateCommunity");
        requestParameters.put("name", "community");
        requestParameters.put("description", "description");

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testCommunity() {
        var controller = controller("community");
        var community = mockCommunity();
        var popularPosts = List.of(mockPost(), mockPost(), mockPost());
        var subscribers = 10L;
        requestParameters.put("name", community.name);
        when(communities.getByName(community.name)).thenReturn(Optional.of(community));
        when(communities.getPopularPosts(community)).thenReturn(popularPosts);
        when(communities.countSubscribers(community)).thenReturn(subscribers);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("community", new CommunityView(community, popularPosts, subscribers, Optional.empty())), result);
    }

    @Test
    public void testCommunityLoggedIn() {
        var controller = controller("community");
        var user = login(mockUser());
        var community = mockCommunity();
        var subscriber = mockSubscriber(user, community);
        var popularPosts = List.of(mockPost(), mockPost(), mockPost());
        var subscribers = 10L;
        requestParameters.put("name", community.name);
        when(communities.getByName(community.name)).thenReturn(Optional.of(community));
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(subscriber));
        when(communities.getPopularPosts(community)).thenReturn(popularPosts);
        when(communities.countSubscribers(community)).thenReturn(subscribers);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("community", new CommunityView(community, popularPosts, subscribers, Optional.of(subscriber))), result);
    }

    @Test
    public void testCommunityNotFound() {
        var controller = controller("community");
        requestParameters.put("name", "test");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("community-name:test"), result);
    }

    @Test
    public void testCommunityPosts() {
        var controller = controller("communityPosts");
        var community = mockCommunity();
        var list = List.of(mockPost(community), mockPost(community));
        requestParameters.put("name", community.name);
        requestParameters.put("day", "1");
        when(communities.getByName(community.name)).thenReturn(Optional.of(community));
        when(communities.getPosts(community, 1)).thenReturn(list);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("community-posts", new CommunityPostsView(community, 1, list)), result);
    }

    @Test
    public void testCommunityPostsNotFound() {
        var controller = controller("communityPosts");
        requestParameters.put("name", "test");
        requestParameters.put("day", "1");

        var result = controller.control(forum, request, responses);

        assertEquals(new NotFound("community-name:test"), result);
    }

    @Test
    public void testSubscribe() {
        var controller = controller("subscribe");
        var user = login(mockUser());
        var community = setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        verify(communities).subscribe(user, community);
        assertEquals(new Redirect("community", "name=" + community.name), result);
    }

    @Test
    public void testSubscribeNotLoggedIn() {
        var controller = controller("subscribe");
        setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("login"), result);
    }

    @Test
    public void testUnsubscribe() {
        var controller = controller("unsubscribe");
        var user = login(mockUser());
        var community = setup(mockCommunity());
        var subscriber = mockSubscriber(user, community);
        when(communities.getSubscriber(user, community)).thenReturn(Optional.of(subscriber));

        var result = controller.control(forum, request, responses);

        verify(communities).unsubscribe(subscriber);
        assertEquals(new Redirect("community", "name=" + community.name), result);
    }

    @Test
    public void testUnsubscribeAgain() {
        var controller = controller("unsubscribe");
        login(mockUser());
        var community = setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        assertEquals(new Redirect("community", "name=" + community.name), result);
    }

    @Test
    public void testManageCommunity() {
        var controller = controller("manageCommunity");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        var subscriber1 = mockSubscriber(community, true);
        var subscriber2 = mockSubscriber(community, true);
        var moderators = List.of(subscriber1, subscriber2);
        when(communities.getModerators(community)).thenReturn(moderators);

        var result = controller.control(forum, request, responses);

        assertEquals(new View("manage-community", new ManageCommunityView(community, moderators, false)), result);
    }

    @Test
    public void testManageCommunityNotOwner() {
        var controller = controller("manageCommunity");
        login(mockUser());
        var community = setup(mockCommunity());

        var result = controller.control(forum, request, responses);

        assertEquals(new Forbidden("not-community-owner:" + community.id), result);
    }

    @Test
    public void testAddModerator() {
        var controller = controller("addModerator");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        var moderator = mockUser();
        var subscriber = mockSubscriber(moderator, community);
        requestParameters.put("name", moderator.name);
        when(users.getByName(moderator.name)).thenReturn(Optional.of(moderator));
        when(communities.getSubscriber(moderator, community)).thenReturn(Optional.of(subscriber));

        var result = controller.control(forum, request, responses);

        verify(communities).addModerator(subscriber);
        assertEquals(new Redirect("manage-community", "community=" + community.id), result);
    }

    @Test
    public void testRemoveModerator() {
        var controller = controller("removeModerator");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        var moderator = mockUser();
        var subscriber = mockSubscriber(moderator, community);
        requestParameters.put("name", moderator.name);
        when(users.getByName(moderator.name)).thenReturn(Optional.of(moderator));
        when(communities.getSubscriber(moderator, community)).thenReturn(Optional.of(subscriber));

        var result = controller.control(forum, request, responses);

        verify(communities).removeModerator(subscriber);
        assertEquals(new Redirect("manage-community", "community=" + community.id), result);
    }

    @Test
    public void testUpdateCommunityDescription() {
        var controller = controller("updateCommunityDescription");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        requestParameters.put("description", "test");

        var result = controller.control(forum, request, responses);

        verify(communities).setDescription(community, "test");
        assertEquals(new Redirect("manage-community", "community=" + community.id), result);
    }

    @Test
    public void testDeleteCommunity() {
        var controller = controller("deleteCommunity");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        requestParameters.put("name-confirm", community.name);

        var result = controller.control(forum, request, responses);

        verify(communities).delete(community);
        assertEquals(new Redirect("index"), result);
    }

    @Test
    public void testDeleteCommunityNotConfirmed() {
        var controller = controller("deleteCommunity");
        var user = login(mockUser());
        var community = setup(mockCommunity(user));
        requestParameters.put("name-confirm", community.name + "1");

        var result = controller.control(forum, request, responses);

        verify(communities, never()).delete(any());
        assertEquals(new Redirect("manage-community", "community=" + community.id), result);
    }
}
