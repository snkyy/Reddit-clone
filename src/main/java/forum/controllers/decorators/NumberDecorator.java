package forum.controllers.decorators;

import forum.logic.Forum;
import forum.controllers.Controller;
import forum.controllers.RequestContext;
import forum.controllers.Responses;

// wymagany parametr (lub kilka parametrów) o danej nazwie i wartości w postaci liczby
public class NumberDecorator<R> extends DecoratorBase<R> {
    private final String parameters;

    private NumberDecorator(Controller<R> controller, String parameters) {
        super(controller);
        this.parameters = parameters;
    }

    public static <R> Controller<R> decorate(Controller<R> controller, String parameter) {
        controller = new NumberDecorator<>(controller, parameter);
        controller = ParameterDecorator.decorate(controller, parameter);
        return controller;
    }

    @Override
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        for (var parameter : parameters.split(",")) {
            try {
                var number = Long.parseLong(request.parameters.get(parameter));
                request.numbers.put(parameter, number);
            } catch (NumberFormatException exception) {
                return responses.badRequest("invalid-parameter:" + parameter);
            }
        } return controller.control(forum, request, responses);
    }
}
