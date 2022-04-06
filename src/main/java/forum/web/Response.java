package forum.web;

import lombok.Value;

import java.util.HashMap;
import java.util.Map;

// dane o odpowiedzi (HTTP) przekazywane serwerowi
@Value
public class Response {
    public Status status;
    public String content;
    public Map<String, String> newCookies = new HashMap<>();

    public enum Status {
        Ok,
        Redirect, // w przypadku tego statusu pole content zawiera adres docelowy oraz parametry w oddzielnych linijkach
        NotFound,
        BadRequest,
        Forbidden,
        Error
    }
}
