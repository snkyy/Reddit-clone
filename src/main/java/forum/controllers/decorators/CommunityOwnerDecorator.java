package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany zalogowany użytkownik i parametr "community" z id społeczności,
// której tej ten użytkownik jest właścicielem
public class CommunityOwnerDecorator<R> extends DecoratorBase<R> {
    private CommunityOwnerDecorator(Controller<R> controller) {
        super(controller);
    }

    @SuppressWarnings("unused")
    public static <R> Controller<R> decorate(Controller<R> controller) {
        controller = new CommunityOwnerDecorator<>(controller);
        controller = LoginDecorator.decorate(controller);
        controller = CommunityDecorator.decorate(controller);
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        var user = request.loggedInUser;
        var community = request.community;
        if (!user.equals(community.owner))
            return responses.forbidden("not-community-owner:" + community.id);
        return controller.control(forum, request, responses);
    }
}
