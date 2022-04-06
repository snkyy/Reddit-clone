package forum.controllers;

import forum.logic.Forum;
import forum.controllers.annotations.Action;
import forum.controllers.annotations.Page;
import forum.controllers.decorators.Decorators;
import lombok.AllArgsConstructor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

// pomocnicza klasa, która "zamienia" metody na kontrolery za pomocą refleksji;
// wykorzystywana do metod w UserControllers, CommunityControlers, PostControllers i CommentControllers
@AllArgsConstructor
public class ControllerMethod<R> implements Controller<R> {
    private final Type type;
    private final String name;
    private final Constructor<?> constructor;
    private final Method method;

    public static <R> Controller<R> build(Class<?> cls, String methodName) {
        Constructor<?> constructor;
        Method method;
        Type type;

        try {
            // argumenty przekazywane metodzie control() są polami klasy, w której znajduje się dana metoda;
            // przy każdym wykonaniu control() jest tworzony nowy obiekt tej klasy z odpowiednimi argumentami
            // i na tym obiekcie jest wykonywana dana metoda
            constructor = cls.getConstructor(Forum.class, RequestContext.class, Responses.class);
            method = cls.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // ustalenie typu kontrolera na podstawie adnotacji Page lub Action
        if (method.isAnnotationPresent(Page.class))
            type = Type.Page;
        else if (method.isAnnotationPresent(Action.class))
            type = Type.Action;
        else
            throw new RuntimeException();

        // nazwa kontrolera to nazwa metody zamieniona z camelCase na hyphen-case
        var name = methodName;
        for (var chr = 'A'; chr <= 'Z'; chr++)
            name = name.replace(Character.toString(chr), "-" + Character.toLowerCase(chr));

        Controller<R> controller = new ControllerMethod<>(type, name, constructor, method);

        // opakowanie kontrolera w dekoratory na podstawie adnotacji
        for (var annotation : method.getAnnotations())
            controller = Decorators.decorate(controller, annotation);

        return controller;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public R control(Forum forum, RequestContext request, Responses<R> responses) {
        try {
            var object = constructor.newInstance(forum, request, responses);
            return (R) method.invoke(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
