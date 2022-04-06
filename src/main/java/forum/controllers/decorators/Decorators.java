package forum.controllers.decorators;

import forum.controllers.Controller;
import forum.controllers.annotations.*;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

// pomocnicza klasa opakowująca kontrolery w dekoratory na podstawie adnotacji
public class Decorators {
    private static final Map<Class<?>, Class<?>> decorators = new HashMap<>();

    // mapowanie adnotacji do klas dekoratorów
    static {
        decorators.put(ParameterRequired.class, ParameterDecorator.class);
        decorators.put(NumberRequired.class, NumberDecorator.class);
        decorators.put(LoginOptional.class, OptionalLoginDecorator.class);
        decorators.put(LoginRequired.class, LoginDecorator.class);
        decorators.put(CommunityRequired.class, CommunityDecorator.class);
        decorators.put(CommunityOwnerRequired.class, CommunityOwnerDecorator.class);
        decorators.put(PostRequired.class, PostDecorator.class);
        decorators.put(CommentRequired.class, CommentDecorator.class);
    }

    @SuppressWarnings("unchecked")
    public static <R> Controller<R> decorate(Controller<R> controller, Annotation annotation) {
        var annotationClass = annotation.annotationType();
        if (!decorators.containsKey(annotationClass))
            return controller;
        var decoratorClass = decorators.get(annotationClass);

        // przypadek, gdy dekorator nie przyjmuje argumentów,
        // czyli metoda decorate ma sygnaturę decorate(Controller)
        try {
            var decorateMethod = decoratorClass.getMethod("decorate", Controller.class);
            return (Controller<R>) decorateMethod.invoke(null, controller);
        } catch (NoSuchMethodException ignored) {
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }

        // przypadek, gdy dekorator przyjmuje jeden argument typu String,
        // czyli metoda decorate ma sygnaturę decorate(Controller, String);
        // wtedy wartość tego argumentu jest odczytywana z adnotacji
        try {
            var decorateMethod = decoratorClass.getMethod("decorate", Controller.class, String.class);
            var valueMethod = annotationClass.getMethod("value");
            var value = (String) valueMethod.invoke(annotation);
            return (Controller<R>) decorateMethod.invoke(null, controller, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
