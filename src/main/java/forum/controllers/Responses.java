package forum.controllers;

// funkcje używane przez kontrolery służące do konstruowania odpowiezi na zapytania;
// kontrolery nie znają typu odpowiedzi, więc ten interfejs jest polimorficzny
public interface Responses<R> {
    R view(String template, Object data); // nazwa szablonu i konkretne dane
    R redirect(String controllerName, String... parameters); // przekierowanie do innego kontrolera; parametry w postaci key=value
    R notFound(String what); // nie znaleziono obiektu
    R badRequest(String message); // złe zapytanie (na przykład brakujący parametr)
    R forbidden(String message); // brak uprawnień do strony
}
