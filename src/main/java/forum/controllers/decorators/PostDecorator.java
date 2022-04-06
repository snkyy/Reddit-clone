package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany parametr "post" z id postu
public class PostDecorator<R> extends DecoratorBase<R> {
    private PostDecorator(Controller<R> controller) {
        super(controller);
    }

    @SuppressWarnings("unused")
    public static <R> Controller<R> decorate(Controller<R> controller) {
        controller = new PostDecorator<>(controller);
        controller = NumberDecorator.decorate(controller, "post");
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        var postId = request.numbers.get("post");
        var optional = forum.posts().get(postId);
        if (optional.isEmpty())
            return responses.notFound("post:" + postId);
        request.post = optional.get();
        return controller.control(forum, request, responses);
    }
}
