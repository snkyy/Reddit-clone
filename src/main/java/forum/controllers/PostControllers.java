package forum.controllers;

import forum.logic.Forum;
import forum.controllers.annotations.*;
import forum.forms.PostSendForm;
import forum.views.PostView;
import forum.views.SendPostView;
import lombok.AllArgsConstructor;

// kontrolery związane z postami
@AllArgsConstructor
@SuppressWarnings("unused")
public class PostControllers<R> {
    private final Forum forum;
    private final RequestContext request;
    private final Responses<R> responses;

    @Page
    @LoginOptional
    @PostRequired
    public R post() {
        var post = request.post;
        var comments = forum.posts().getComments(post);
        var votes = forum.posts().countVotes(post);

        var user = request.optionalLoggedInUser;
        var loggedIn = user.isPresent();
        var sender = user.map(u -> u.equals(post.sender)).orElse(false);
        var subscriber = user.flatMap(u -> forum.communities().getSubscriber(u, post.community));
        var moderator = subscriber.map(s -> s.moderator).orElse(false);
        var vote = user.flatMap(u -> forum.posts().getVote(u, post));

        return responses.view("post", new PostView(post, comments, votes, loggedIn, sender, moderator, vote));
    }

    @Page
    @LoginRequired
    @CommunityRequired
    public R sendPost() {
        var user = request.loggedInUser;
        var community = request.community;
        return responses.view("send-post", new SendPostView(user, community));
    }

    @Action
    @LoginRequired
    @CommunityRequired
    @ParameterRequired("title,content")
    public R doSendPost() {
        var community = request.community;
        var title = request.parameters.get("title");
        var content = request.parameters.get("content");

        var form = new PostSendForm(community, title, content);
        forum.posts().send(request.loggedInUser, form);

        return responses.redirect("community", "name=" + community.name);
    }

    @Action
    @LoginRequired
    @PostRequired
    @ParameterRequired("content")
    public R editPost() {
        var post = request.post;
        var content = request.parameters.get("content");

        if (!post.sender.equals(request.loggedInUser))
            return responses.forbidden("not-post-sender:" + post.id);

        forum.posts().edit(post, content);
        return responses.redirect("post", "post=" + post.id);
    }

    @Action
    @LoginRequired
    @PostRequired
    public R deletePost() {
        var user = request.loggedInUser;
        var post = request.post;
        var confirm  = request.parameters.containsKey("confirm");

        if (!confirm)
            return responses.redirect("post", "post=" + post.id);

        var sender = post.sender.equals(request.loggedInUser);
        var moderator = forum.communities().getSubscriber(user, post.community).map(s -> s.moderator).orElse(false);

        if (!sender && !moderator)
            return responses.forbidden("not-post-sender:" + post.id);

        forum.posts().delete(post, moderator && !sender);
        return responses.redirect("community", "name=" + post.community.name);
    }

    @Action
    @LoginRequired
    @PostRequired
    public R upvotePost() {
        var user = request.loggedInUser;
        var post = request.post;
        forum.posts().upvote(user, post);
        return responses.redirect("post", "post=" + post.id);
    }

    @Action
    @LoginRequired
    @PostRequired
    public R downvotePost() {
        var user = request.loggedInUser;
        var post = request.post;
        forum.posts().downvote(user, post);
        return responses.redirect("post", "post=" + post.id);
    }

    @Action
    @LoginRequired
    @PostRequired
    public R unvotePost() {
        var user = request.loggedInUser;
        var post = request.post;
        var vote = forum.posts().getVote(user, post);
        vote.ifPresent(v -> forum.posts().unvote(v));
        return responses.redirect("post", "post=" + post.id);
    }
}
