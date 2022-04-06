package forum.controllers;

import forum.logic.Forum;
import forum.controllers.annotations.*;
import forum.forms.CommentSendForm;
import forum.views.CommentView;
import forum.views.SendCommentView;
import forum.views.SendReplyView;
import lombok.AllArgsConstructor;

import java.util.Optional;

// kontrolery związane z komentarzami
@AllArgsConstructor
@SuppressWarnings("unused")
public class CommentControllers<R> {
    private final Forum forum;
    private final RequestContext request;
    private final Responses<R> responses;

    @Page
    @LoginOptional
    @CommentRequired
    public R comment() {
        var comment = request.comment;
        var tree = forum.comments().getTree(comment);
        var parent = forum.comments().getParent(comment);
        var votes = forum.comments().countVotes(comment);

        var user = request.optionalLoggedInUser;
        var loggedIn = user.isPresent();
        var sender = user.map(u -> u.equals(comment.sender)).orElse(false);
        var subscriber = user.flatMap(u -> forum.communities().getSubscriber(u, comment.post.community));
        var moderator = subscriber.map(s -> s.moderator).orElse(false);
        var vote = user.flatMap(u -> forum.comments().getVote(u, comment));

        return responses.view("comment", new CommentView(tree, parent, votes, loggedIn, sender, moderator, vote));
    }

    @Page
    @LoginRequired
    @PostRequired
    public R sendComment() {
        var user = request.loggedInUser;
        var post = request.post;
        return responses.view("send-comment", new SendCommentView(user, post));
    }

    @Page
    @LoginRequired
    @CommentRequired
    public R sendReply() {
        var user = request.loggedInUser;
        var comment = request.comment;
        return responses.view("send-reply", new SendReplyView(user, comment));
    }

    @Action
    @LoginRequired
    @PostRequired
    @ParameterRequired("content")
    public R doSendComment() {
        var user = request.loggedInUser;
        var post = request.post;
        var content = request.parameters.get("content");

        var form = new CommentSendForm(post, content, Optional.empty());
        forum.comments().send(user, form);

        return responses.redirect("post", "post=" + post.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    @ParameterRequired("content")
    public R doSendReply() {
        var user = request.loggedInUser;
        var comment = request.comment;
        var post = comment.post;
        var content = request.parameters.get("content");

        var form = new CommentSendForm(post, content, Optional.of(comment));
        forum.comments().send(user, form);

        return responses.redirect("post", "post=" + post.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    @ParameterRequired("content")
    public R editComment() {
        var comment = request.comment;
        var content = request.parameters.get("content");

        if (!comment.sender.equals(request.loggedInUser))
            return responses.forbidden("not-comment-sender:" + comment.id);

        forum.comments().edit(comment, content);
        return responses.redirect("comment", "comment=" + comment.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    public R deleteComment() {
        var user = request.loggedInUser;
        var comment = request.comment;
        var confirm  = request.parameters.containsKey("confirm");

        if (!confirm)
            return responses.redirect("comment", "comment=" + comment.id);

        var sender = comment.sender.equals(request.loggedInUser);
        var moderator = forum.communities().getSubscriber(user, comment.post.community).map(s -> s.moderator).orElse(false);

        if (!sender && !moderator)
            return responses.forbidden("not-comment-sender:" + comment.id);

        forum.comments().delete(comment, moderator && !sender);
        return responses.redirect("post", "post=" + comment.post.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    public R upvoteComment() {
        var user = request.loggedInUser;
        var comment = request.comment;
        forum.comments().upvote(user, comment);
        return responses.redirect("comment", "comment=" + comment.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    public R downvoteComment() {
        var user = request.loggedInUser;
        var comment = request.comment;
        forum.comments().downvote(user, comment);
        return responses.redirect("comment", "comment=" + comment.id);
    }

    @Action
    @LoginRequired
    @CommentRequired
    public R unvoteComment() {
        var user = request.loggedInUser;
        var comment = request.comment;
        var vote = forum.comments().getVote(user, comment);
        vote.ifPresent(v -> forum.comments().unvote(v));
        return responses.redirect("comment", "comment=" + comment.id);
    }
}
