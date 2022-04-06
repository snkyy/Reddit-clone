package forum.controllers;

import forum.logic.Forum;
import forum.controllers.annotations.*;
import forum.forms.CommunityCreateForm;
import forum.exceptions.CommunityCreateException;
import forum.views.*;
import lombok.AllArgsConstructor;

// kontrolery związane ze społecznościami
@AllArgsConstructor
@SuppressWarnings("unused")
public class CommunityControllers<R> {
    private final Forum forum;
    private final RequestContext request;
    private final Responses<R> responses;

    @Page
    @NumberRequired("page")
    public R communityList() {
        var page = request.numbers.get("page");
        var communities = forum.communities().getCommunities(page);
        return responses.view("community-list", new CommunityListView(page, communities));
    }

    @Page
    @LoginRequired
    public R createCommunity() {
        var nameInvalid = request.parameters.containsKey("name-invalid");
        var nameTaken = request.parameters.containsKey("name-taken");
        return responses.view("create-community", new CreateCommunityView(request.loggedInUser, nameInvalid, nameTaken));
    }

    @Action
    @LoginRequired
    @ParameterRequired("name,description")
    public R doCreateCommunity() {
        var name = request.parameters.get("name");
        var description = request.parameters.get("description");
        var form = new CommunityCreateForm(name, description);

        try {
            var community = forum.communities().create(request.loggedInUser, form);
            return responses.redirect("community", "name=" + community.name);
        }

        catch (CommunityCreateException e) {
            switch (e.cause) {
                case NameInvalid:
                    return responses.redirect("create-community", "name-invalid=true");
                case NameTaken:
                    return responses.redirect("create-community", "name-taken=true");
                default:
                    throw new RuntimeException(e);
            }
        }
    }

    @Page
    @LoginOptional
    @ParameterRequired("name")
    public R community() {
        var name = request.parameters.get("name");
        var optional = forum.communities().getByName(name);
        if (optional.isEmpty())
            return responses.notFound("community-name:" + name);
        var community = optional.get();
        var popularPosts = forum.communities().getPopularPosts(community);
        var subscribers = forum.communities().countSubscribers(community);
        var loggedInSubscriber = request.optionalLoggedInUser.flatMap(u -> forum.communities().getSubscriber(u, community));
        return responses.view("community", new CommunityView(community, popularPosts, subscribers, loggedInSubscriber));
    }

    @Page
    @ParameterRequired("name")
    @NumberRequired("day")
    public R communityPosts() {
        var name = request.parameters.get("name");
        var day = request.numbers.get("day");
        var optional = forum.communities().getByName(name);
        if (optional.isEmpty())
            return responses.notFound("community-name:" + name);
        var community = optional.get();
        var posts = forum.communities().getPosts(community, day);
        return responses.view("community-posts", new CommunityPostsView(community, day, posts));
    }

    @Action
    @LoginRequired
    @CommunityRequired
    public R subscribe() {
        var user = request.loggedInUser;
        var community = request.community;
        forum.communities().subscribe(user, community);
        return responses.redirect("community", "name=" + community.name);
    }

    @Action
    @LoginRequired
    @CommunityRequired
    public R unsubscribe() {
        var user = request.loggedInUser;
        var community = request.community;
        var subscriber = forum.communities().getSubscriber(user, community);
        subscriber.ifPresent(s -> forum.communities().unsubscribe(s));
        return responses.redirect("community", "name=" + community.name);
    }

    @Page
    @CommunityOwnerRequired
    public R manageCommunity() {
        var community = request.community;
        var moderators = forum.communities().getModerators(community);
        var subscriberNotFound = request.parameters.containsKey("subscriber-not-found");
        return responses.view("manage-community", new ManageCommunityView(community, moderators, subscriberNotFound));
    }

    @Action
    @CommunityOwnerRequired
    @ParameterRequired("name")
    public R addModerator() {
        var community = request.community;
        var name = request.parameters.get("name");
        var user = forum.users().getByName(name);
        var subscriber = user.flatMap(u -> forum.communities().getSubscriber(u, community));
        if (subscriber.isEmpty())
            return responses.redirect("manage-community", "community=" + community.id, "subscriber-not-found=true");
        forum.communities().addModerator(subscriber.get());
        return responses.redirect("manage-community", "community=" + community.id);
    }

    @Action
    @CommunityOwnerRequired
    @ParameterRequired("name")
    public R removeModerator() {
        var community = request.community;
        var name = request.parameters.get("name");
        var user = forum.users().getByName(name);
        var subscriber = user.flatMap(u -> forum.communities().getSubscriber(u, community));
        if (subscriber.isEmpty())
            return responses.redirect("manage-community", "community=" + community.id, "subscriber-not-found=true");
        forum.communities().removeModerator(subscriber.get());
        return responses.redirect("manage-community", "community=" + community.id);
    }

    @Action
    @CommunityOwnerRequired
    @ParameterRequired("description")
    public R updateCommunityDescription() {
        var community = request.community;
        var description = request.parameters.get("description");
        forum.communities().setDescription(community, description);
        return responses.redirect("manage-community", "community=" + community.id);
    }

    @Action
    @CommunityOwnerRequired
    @ParameterRequired("name-confirm")
    public R deleteCommunity() {
        var community = request.community;
        var nameConfirm = request.parameters.get("name-confirm");
        if (!community.name.equals(nameConfirm))
            return responses.redirect("manage-community", "community=" + community.id);
        forum.communities().delete(community);
        return responses.redirect("index");
    }
}
