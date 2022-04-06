package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany parametr "community" z id społeczności
public class CommunityDecorator<R> extends DecoratorBase<R> {
    private CommunityDecorator(Controller<R> controller) {
        super(controller);
    }

    public static <R> Controller<R> decorate(Controller<R> controller) {
        controller = new CommunityDecorator<>(controller);
        controller = NumberDecorator.decorate(controller, "community");
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        var communityId = request.numbers.get("community");
        var optional = forum.communities().get(communityId);
        if (optional.isEmpty())
            return responses.notFound("community:" + communityId);
        request.community = optional.get();
        return controller.control(forum, request, responses);
    }
}
