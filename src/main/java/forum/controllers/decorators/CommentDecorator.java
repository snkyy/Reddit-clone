package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany parametr "comment" z id komentarza
public class CommentDecorator<R> extends DecoratorBase<R> {
    private CommentDecorator(Controller<R> controller) {
        super(controller);
    }

    @SuppressWarnings("unused")
    public static <R> Controller<R> decorate(Controller<R> controller) {
        controller = new CommentDecorator<>(controller);
        controller = NumberDecorator.decorate(controller, "comment");
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        var commentId = request.numbers.get("comment");
        var optional = forum.comments().get(commentId);
        if (optional.isEmpty())
            return responses.notFound("comment:" + commentId);
        request.comment = optional.get();
        return controller.control(forum, request, responses);
    }
}
