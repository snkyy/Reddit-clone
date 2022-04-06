package forum.controllers;

import forum.logic.Forum;

// kontroler - "abstrakcyjny" opis handlera w serwerze;
// jest polimorficzny po type odpowiedzi na zapytanie;
// ten interfejs jest przeznaczony do implementacji tylko w obrębie pakietu forum.controllers
public interface Controller<R> {
    Type type(); // rodzaj mapuje się na metodę zapytania
    String name(); // nazwa mapuje się na ścieżkę zapytania
    R control(Forum forum, RequestContext request, Responses<R> responses);

    enum Type {
        Page, // wyświetlenie strony
        Action // wykonanie czynności
    }
}
