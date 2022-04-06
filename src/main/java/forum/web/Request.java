package forum.web;

import lombok.Value;

import java.util.Map;

// dane o zapytaniu (HTTP) dostarczane przez serwer
@Value
public class Request {
    public Method method;
    public String path;
    public Map<String, String> parameters; // tylko do odczytu
    public Map<String, String> cookies; // tylko do odczytu

    public enum Method {
        Get,
        Post
    }
}
