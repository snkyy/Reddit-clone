package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany zalogowany użytkownik
public class LoginDecorator<R> extends DecoratorBase<R> {
    private LoginDecorator(Controller<R> controller) {
        super(controller);
    }

    public static <R> Controller<R> decorate(Controller<R> controller) {
        controller = new LoginDecorator<>(controller);
        controller = OptionalLoginDecorator.decorate(controller);
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        if (request.optionalLoggedInUser.isEmpty())
            return responses.redirect("login");
        request.loggedInUser = request.optionalLoggedInUser.get();
        return controller.control(forum, request, responses);
    }
}
