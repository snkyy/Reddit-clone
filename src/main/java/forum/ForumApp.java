package forum;

import forum.controllers.*;
import forum.logic.Forum;
import forum.services.Emailer;
import forum.views.ErrorView;
import forum.web.*;
import lombok.AllArgsConstructor;

// główna klasa łącząca pakiety forum.logic, forum.controllers i forum.web;
// tworzy wszystkie kontrolery i uruchamia serwer z handlerami,
// które wykonują te kontrolery przekazując im obiekt Forum
@AllArgsConstructor
public class ForumApp {
    private final Forum forum;
    private final Emailer emailer;
    private final Server server;
    private final SessionManager sessionManager;
    private final TemplateProcessor templateProcessor;

    // uruchamia aplikację
    public void run() {
        addController(UserControllers.class, "index");
        addController(UserControllers.class, "login");
        addController(UserControllers.class, "doLogin");
        addController(UserControllers.class, "logout");
        addController(UserControllers.class, "register");
        addController(UserControllers.class, "doRegister");
        addController(UserControllers.class, "user");
        addController(UserControllers.class, "account");
        addController(UserControllers.class, "updateEmail");
        addController(UserControllers.class, "updatePassword");
        addController(UserControllers.class, "deleteAccount");

        addController(CommunityControllers.class, "communityList");
        addController(CommunityControllers.class, "createCommunity");
        addController(CommunityControllers.class, "doCreateCommunity");
        addController(CommunityControllers.class, "community");
        addController(CommunityControllers.class, "communityPosts");
        addController(CommunityControllers.class, "subscribe");
        addController(CommunityControllers.class, "unsubscribe");
        addController(CommunityControllers.class, "manageCommunity");
        addController(CommunityControllers.class, "addModerator");
        addController(CommunityControllers.class, "removeModerator");
        addController(CommunityControllers.class, "updateCommunityDescription");
        addController(CommunityControllers.class, "deleteCommunity");

        addController(PostControllers.class, "post");
        addController(PostControllers.class, "sendPost");
        addController(PostControllers.class, "doSendPost");
        addController(PostControllers.class, "editPost");
        addController(PostControllers.class, "deletePost");
        addController(PostControllers.class, "upvotePost");
        addController(PostControllers.class, "downvotePost");
        addController(PostControllers.class, "unvotePost");

        addController(CommentControllers.class, "comment");
        addController(CommentControllers.class, "sendComment");
        addController(CommentControllers.class, "sendReply");
        addController(CommentControllers.class, "doSendComment");
        addController(CommentControllers.class, "doSendReply");
        addController(CommentControllers.class, "editComment");
        addController(CommentControllers.class, "deleteComment");
        addController(CommentControllers.class, "upvoteComment");
        addController(CommentControllers.class, "downvoteComment");
        addController(CommentControllers.class, "unvoteComment");

        addIndexRedirect();

        runServer();
    }

    private void addController(Class<?> cls, String methodName) {
        addController(ControllerMethod.build(cls, methodName));
    }

    // dodaje kontroler jako handler dla serwera
    private void addController(Controller<Response> controller) {
        var method = requestMethod(controller.type());
        var path = requestPath(controller.name());
        var handler = controllerHandler(controller);
        server.addHandler(method, path, handler);
    }

    // mapowanie rodzaju kontrolera na metodę zapytania
    private Request.Method requestMethod(Controller.Type type) {
        switch (type) {
            case Page:
                return Request.Method.Get;
            case Action:
                return Request.Method.Post;
            default:
                throw new RuntimeException();
        }
    }

    // mapowanie nazwy kontrolera na ścieżkę zapytania
    private String requestPath(String controllerName) {
        return "/" + controllerName;
    }

    // tworzy handler na podstawie kontrolera
    private Server.Handler controllerHandler(Controller<Response> controller) {
        return errorWrapper(request -> {
            var session = sessionManager.getSession(request);
            var context = new RequestContext(request.parameters, session.store);
            var response = controller.control(forum, context, responses);
            sessionManager.saveSession(response, session);
            return response;
        });
    }

    // dekorator handlerów łapiący wyjątki
    private Server.Handler errorWrapper(Server.Handler handler) {
        return request -> {
            try {
                return handler.handle(request);
            } catch (Exception e) {
                e.printStackTrace();
                emailer.emailAdmin("error");
                return new Response(Response.Status.Error, "<h1>Internal Server Error</h1>");
            }
        };
    }

    // przekierowanie z / do /index
    private void addIndexRedirect() {
        var handler = errorWrapper(request -> responses.redirect("index"));
        server.addHandler(Request.Method.Get, "/", handler);
    }

    // domyślny handler wyświetlający błąd o nieznalezionej stronie
    private Server.Handler pageNotFoundHandler() {
        return errorWrapper(request -> responses.notFound("page:" + request.path));
    }

    // uruchamia serwer
    private void runServer() {
        emailer.emailAdmin("start");

        var defaultHandler = pageNotFoundHandler();
        server.run(defaultHandler);

        emailer.emailAdmin("stop");
    }

    // implementacja forum.controllers.Responses przekazywana kontrolerom
    private final Responses<Response> responses = new Responses<>() {
        @Override
        public Response view(String template, Object data) {
            var content = templateProcessor.process(template, data);
            return new Response(Response.Status.Ok, content);
        }

        @Override
        public Response redirect(String controllerName, String... parameters) {
            var content = new StringBuilder();
            content.append(requestPath(controllerName)).append("\n");
            for (var parameter : parameters)
                content.append(parameter).append("\n");
            return new Response(Response.Status.Redirect, content.toString());
        }

        @Override
        public Response notFound(String what) {
            var view = new ErrorView("not-found", what);
            var content = templateProcessor.process("error", view);
            return new Response(Response.Status.NotFound, content);
        }

        @Override
        public Response badRequest(String message) {
            var view = new ErrorView("bad-request", message);
            var content = templateProcessor.process("error", view);
            return new Response(Response.Status.BadRequest, content);
        }

        @Override
        public Response forbidden(String message) {
            var view = new ErrorView("forbidden", message);
            var content = templateProcessor.process("error", view);
            return new Response(Response.Status.Forbidden, content);
        }
    };
}
