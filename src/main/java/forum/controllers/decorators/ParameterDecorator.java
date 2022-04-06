package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany parametr (lub kilka parametrów) o danej nazwie
public class ParameterDecorator<R> extends DecoratorBase<R> {
    private final String parameters;

    private ParameterDecorator(Controller<R> controller, String parameters) {
        super(controller);
        this.parameters = parameters;
    }

    public static <R> Controller<R> decorate(Controller<R> controller, String parameter) {
        return new ParameterDecorator<>(controller, parameter);
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        for (var parameter : parameters.split(",")) {
            if (!request.parameters.containsKey(parameter))
                return responses.badRequest("missing-parameter:" + parameter);
        } return controller.control(forum, request, responses);
    }
}
