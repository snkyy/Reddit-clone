package forum.controllers;

import forum.entities.Comment;
import forum.entities.Community;
import forum.entities.Post;
import forum.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// dane zapytania wykorzystywane przez kontrolery
public class RequestContext {
    public final Map<String, String> parameters;
    public final Map<String, String> sessionStore;

    public RequestContext(Map<String, String> parameters, Map<String, String> sessionStore) {
        this.parameters = parameters;
        this.sessionStore = sessionStore;
    }

    // poniższe pola pełnią funkcję "schowka" na różne dane,
    // które dekoratory przekazują właściwemu kontrolerowi;
    // użycie publicznych i modyfikowalnych pól z pewnością nie jest zbyt eleganckie,
    // ale najlepiej mysleć o tym jak o mapie Map<String, Object> z prealokowanymi slotami;
    // ponieważ interfejs Controller nie jest przeznaczony do implementacji od zewnątrz
    // oraz wszystkie kontrolery są w pakiecie forum.controllers,
    // to takie "udogodnienie" nie jest szczególnie szkodliwe
    public final Map<String, Long> numbers = new HashMap<>();
    public Optional<User> optionalLoggedInUser;
    public User loggedInUser;
    public Community community;
    public Post post;
    public Comment comment;
}
