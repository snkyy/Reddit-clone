package impl;

import forum.web.Request;
import forum.web.Response;
import forum.web.Session;
import forum.web.SessionManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

// prosty menedżer sesji używający HashMap;
// nie jest on praktyczny do większych zastosowań,
// ponieważ trzyma wszystkie dane w pamięci
// i nie usuwa ich z upływem czasu
public class SimpleSessionManager implements SessionManager {
    private final Random random = new Random();
    private final Map<Long, Session> sessions = new HashMap<>();

    @Override
    public synchronized Session getSession(Request request) {
        if (request.cookies.containsKey("session")) { // odczytanie identyfikatora sesji z ciastka
            try {
                var id = Long.parseLong(request.cookies.get("session"));
                if (sessions.containsKey(id))
                    return sessions.get(id); // zwrócenie wcześniej utworzonego obiektu Session
            } catch (NumberFormatException ignored) { }
        }

        long id;
        do { // znalezienie wolnego losowego identyfikatora sesji
            id = random.nextLong();
        } while (sessions.containsKey(id));

        var session = new Session(id); // stworzenie nowego obiektu Session
        sessions.put(session.id, session);
        return session;
    }

    // w tej implementacji nie trzeba specjalnie utrwalać danych sesji w saveSession(),
    // ponieważ getSession() może wielokrotnie zwracać ten sam obiekt Session
    @Override
    public void saveSession(Response response, Session session) {
        response.newCookies.put("session", Long.toString(session.id)); // zapisanie identyfikatora sesji do ciastka
    }
}
