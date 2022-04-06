package forum.controllers.decorators;

import forum.controllers.Controller;

// bazowa klasa dla dekoratorów kontrolerów
public abstract class DecoratorBase<R> implements Controller<R> {
    protected final Controller<R> controller;

    public DecoratorBase(Controller<R> controller) {
        this.controller = controller;
    }

    @Override
    public Type type() {
        return controller.type();
    }

    @Override
    public String name() {
        return controller.name();
    }
}
