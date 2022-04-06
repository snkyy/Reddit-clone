package forum.web;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

// klasa reprezentująca sesję użytkownika;
// pole store służy do przechowywania dowolnych danych;
// jest wypełniane przez SessionManager.getSession(),
// czytane i modyfikowane przez handlery,
// i utrwalane (na przykład do bazy danych) przez SessionManager.saveSession()
@Value
public class Session {
    public long id; // identyfikator do użytku przez menedżer sesji
    public Map<String, String> store = new HashMap<>();
}
