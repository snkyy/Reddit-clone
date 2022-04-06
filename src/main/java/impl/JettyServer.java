package impl;

import forum.web.Request;
import forum.web.Response;
import forum.web.Server;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

// serwer Jetty
public class JettyServer implements Server {
    private final org.eclipse.jetty.server.Server server;
    private final ServletContextHandler context;

    private static final int port = 8080;

    public JettyServer() {
        server = new org.eclipse.jetty.server.Server(port);
        context = new ServletContextHandler(server, "/");
    }

    @Override
    public void addHandler(Request.Method method, String path, Handler handler) {
        var acceptGet = method == Request.Method.Get;
        var acceptPost = method == Request.Method.Post;
        var servlet = new ServletHolder(new Servlet(acceptGet, acceptPost, handler));
        context.addServlet(servlet, path.equals("/") ? "" : path); // Jetty specjalnie traktuje ścieżkę /
    }

    @Override
    public void run(Handler defaultHandler) {
        var servlet = new ServletHolder(new Servlet(true, true, defaultHandler));
        context.addServlet(servlet, "/*");

        try {
            server.start();
            server.join();
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // serwlet wykonujący dany handler
    @AllArgsConstructor
    private static class Servlet extends HttpServlet {
        private final boolean acceptGet;
        private final boolean acceptPost;
        private final Server.Handler handler;

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (acceptGet)
                handle(true, req, resp);
            else
                super.doGet(req, resp);
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (acceptPost)
                handle(false, req, resp);
            else
                super.doPost(req, resp);
        }

        // konstruuje Request na podstawie HttpServletRequest, wykonuje handler, i zapisuje Response do HttpServletResponse
        private void handle(boolean get, HttpServletRequest req, HttpServletResponse resp) throws IOException {
            var method = get ? Request.Method.Get : Request.Method.Post;
            var path = req.getRequestURI();

            var parameters = new HashMap<String, String>();
            for (var entry : req.getParameterMap().entrySet())
                parameters.put(entry.getKey(), entry.getValue()[0]); // wielokrotne parametry są ignorowane

            var cookies = new HashMap<String, String>();
            for (var cookie : Optional.ofNullable(req.getCookies()).orElse(new Cookie[0]))
                cookies.put(cookie.getName(), cookie.getValue());

            var request = new Request(method, path, parameters, cookies);
            var response = handler.handle(request);

            for (var entry : response.newCookies.entrySet())
                cookies.put(entry.getKey(), entry.getValue());
            for (var entry : cookies.entrySet())
                resp.addCookie(new Cookie(entry.getKey(), entry.getValue()));

            if (response.status == Response.Status.Redirect) { // specjalne traktowanie statusu Redirect
                var lines = response.content.lines().toArray(String[]::new);
                var location = redirectLocation(lines);
                resp.sendRedirect(location);
            } else {
                var status = httpStatus(response.status);
                resp.setStatus(status.getCode());
                resp.setContentType("text/html");
                resp.getWriter().write(response.content);
            }
        }

        // konstruuje ścieżkę zapytania z parametrami w standardowym formacie URL
        private String redirectLocation(String[] lines) {
            var location = new StringBuilder();
            for (var i = 0; i < lines.length; i++) {
                if (i > 0)
                    location.append(i == 1 ? '?' : '&');
                location.append(lines[i]);
            } return location.toString();
        }

        // mapowanie statusów HTTP
        private HttpStatus.Code httpStatus(Response.Status status) {
            switch (status) {
                case Ok:
                    return HttpStatus.Code.OK;
                case NotFound:
                    return HttpStatus.Code.NOT_FOUND;
                case BadRequest:
                    return HttpStatus.Code.BAD_REQUEST;
                case Forbidden:
                    return HttpStatus.Code.FORBIDDEN;
                case Error:
                    return HttpStatus.Code.INTERNAL_SERVER_ERROR;
                default:
                    throw new RuntimeException();
            }
        }
    }
}
