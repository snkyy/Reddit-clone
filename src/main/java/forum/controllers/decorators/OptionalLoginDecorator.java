package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

import java.util.Optional;

// wypełnia pole request.optionalLoggedInUser
public class OptionalLoginDecorator<R> extends DecoratorBase<R> {
    private OptionalLoginDecorator(Controller<R> controller) {
        super(controller);
    }

    public static <R> Controller<R> decorate(Controller<R> controller) {
        return new OptionalLoginDecorator<>(controller);
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        if (request.sessionStore.containsKey("user")) {
            var userId = Long.parseLong(request.sessionStore.get("user"));
            request.optionalLoggedInUser = forum.users().get(userId);
        } else
            request.optionalLoggedInUser = Optional.empty();
        return controller.control(forum, request, responses);
    }
}
