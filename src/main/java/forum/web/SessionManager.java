package forum.web;

// interfejs do menedżera sesji;
// jest on odpowiedzialny za zarządzanie obiektami Session i trzymanie ich danych;
// typowo używa ciastek w Request i Response do kojarzenia użytkowników z sesją
public interface SessionManager {
    Session getSession(Request request); // tworzy obiekt Session na podstawie zapytania
    void saveSession(Response response, Session session); // utrwala dane w Session i modyfikuje odpowiedź aby skojarzyć użytkownika z sesją
}
