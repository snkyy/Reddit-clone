# Opis projektu

Projekt realizuje proste forum internetowe inspirowane [Redditem](https://reddit.com).

Zarejestrowani użytkownicy mogą tworzyć *społeczności*
oraz wysyłać *posty* w tych społecznościach.
Każdy post posiada tytuł oraz zawartość
w postaci tekstu lub linku.
Pod postem użytkownicy mogą dyskutować na jego temat
poprzez dodawanie komentarzy.
Komentarz może być odpowiedzią do innego komentarza,
więc dyskusja pod postem tworzy strukturę drzewa.

Na każdy post lub komentarz
można oddać *głos w górę* lub *głos w dół* (*upvote* lub *downvote*).
Każdy post i komentarz ma pewną liczbę punktów
obliczoną na podstawie głosów
(w tym prostym przypadku jest to różnica liczby głosów w górę i liczby głosów w dół).
Na stronie głównej społeczności są pokazywane najbardziej *popularne* posty.
Popularność postu jest pewną funkcją od liczby punktów i czasu wysłania postu
(im post jest starszy, tym ma mniejszą popularność).

Społeczności można subskrybować.
Na stronie głównej użytkownik widzi listę swoich subskrypcji
oraz kompilację ich popularnych postów (*feed*).

Każda społeczność ma dokładnie jednego właściciela,
który jest jej założycielem.
Właściciel może modyfikować opis społeczności
i dodawać *moderatorów* (sam też jest moderatorem).
Moderatorzy społeczności mogą w niej usuwać nieodpowiednie posty i komentarze.

# Struktura projektu

Pakiet `forum.entities` zawiera klasy-rekordy opisujące
byty występujące w modelu danych, takie jak użytkownik, społeczność, post, itp.
Rekordy te są zwracane przez bazę danych i są tylko do odczytu.

Pakiet `forum.databases` zawiera interfejs bazy danych.
Jest on tematycznie podzielony na cztery interfejsy:
`UsersDatabase`, `CommunitiesDatabase`, `PostsDatabase` i `CommentsDatabase`
(wykorzystywane przez odpowiednie klasy w pakiecie `forum.logic`)
które dla wygody są złączone w jeden interfejs `Database`.
Baza danych jest odpowiedzialna za
tworzenie, znajdowanie, modyfikowanie i usuwanie bytów w modelu danych.
Baza danych tworzy klasy-rekordy z pakietu `forum.entities`.

Pakiet `forum.logic` zawiera klasy realizujące logikę biznesową aplikacji.
Różne czynności wykonywane w aplikacji są tematycznie podzielone
na klasy `Users`, `Communities`, `Posts` i `Comments`.
Są one odpowiedzialne za interakcje z bazą danych według odpowiednich reguł
(na przykład reguły liczenia punktów za oddawane głosy,
albo reguły wyznaczania popularnych postów).
Posiadają jako zależności odpowiednie interfejsy z `forum.databases`
oraz różne interfejsy z `forum.services` i `forum.validators`.
Klasa `Forum` grupuje te cztery klasy.
Klasa `ForumFactory` konstruuje te wszystkie klasy
i wstrzykuje do nich odpowiednie zależności.
Klasy z tego pakietu są testowane.

Pakiet `forum.services` zawiera kilka interfejsów do różnych funkcji:

- `Clock` - zwraca czas w sekundach
- `Authenticator` - hashuje i sprawdza hasła
- `Emailer` - wysyła e-maile

Pakiet `forum.validators` zawiera kilka interfejsów
do sprawdzania poprawności danych wprowadzanych przez użytkowników.

Pakiet `forum.forms` zawiera kilka klas-rekordów
grupujących argumenty do niektórych funkcji w `forum.logic`.

Pakiet `forum.exceptions` zawiera kilka klas-wyjątków
rzucanych przez niektóre funkcje w `forum.logic`.

Pakiet `forum.views` zawiera klasy-rekordy opisujące
dane widoczne na różnych stronach wyświetlanych przez użytkowników.

---

Pakiet `forum.web` zawiera klasy i interfejsy związane z aplikacjami webowymi:

- `Request` - klasa-rekord opisująca zapytanie HTTP
- `Response` - klasa-rekord opisująca odpowiedź HTTP
- `Session` - klasa-rekord zawierająca dane sesji użytkownika
- `Server` - interfejs dla serwera HTTP
- `SessionManager` - interfejs dla menedżera sesji
- `TemplateProcessor` - interfejs dla procesora szablonów

W rzeczywistości serwer może implementować niepełny protokół HTTP lub zupełnie inny protokół.
Wystarczy tylko, że dostarczy funkcjonalność wynikającą z klas `Request` i `Response`.
Użycie tego interfejsu polega na podaniu handlerów
(funkcji otrzymujących `Request` i zwracających `Response`)
i uruchomieniu serwera, który będzie obsługiwał zapytania za pomocą tych handlerów.
Można podawać handlery związane z konkretną ścieżką -
wtedy serwer wykonuje je tylko na zapytaniach z tą ścieżką.
Teoretycznie wystarczyłoby podanie tylko jednego handlera,
który odczytuje ścieżkę z obiektu `Request`
i wykonuje na jej podstawie odpowiedni inny handler,
ale lepiej uznać mapowanie ścieżek do handlerów
za odpowiedzialność serwera.

Menedżer sesji zwraca obiekt `Session` dostając `Request`,
zapisuje zmiany wykonane na tym obiekcie,
oraz odpowiednio modyfikuje `Response`,
aby skojarzyć użytkownika z sesją.
Typowo menedżer sesji odczytuje identyfikator sesji z ciastek w `Request`,
odczytuje dane tej sesji z bazy danych (niekoniecznie utrwalanej na dysku)
tworząc obiekt `Session`,
zapisuje zmodyfikowane dane sesji do bazy danych
i wpisuje identyfikator sesji do ciastek w `Response`.
Alternatywnie może nie używać żadnej bazy danych i zapisywać wszystkie dane
w podpisanych kryptograficznie ciastkach.
Najprostsza implementacja trzyma wszystkie dane w pamięci za pomocą `HashMap`.

Procesor szablonów generuje zawartość strony (typowo HTML)
na podstawie szablonu strony oraz konkretnych danych.
Przy generowaniu zawartości interfejs przyjmuje tylko nazwę szablonu -
wygląd strony i język procesora szablonów jest częścią implementacji.
Konkretne dane muszą być klasą-rekordem z pakietu `forum.views`.

---

Pakiet `forum.controllers` zawiera *kontrolery*,
czyli funkcje obsługujące zapytania webowe
i wykonujące odpowiednie czynności na obiekcie `Forum`.
Każdy kontroler implementuje interfejs `Controller` i deklaruje swoją nazwę
oraz swój rodzaj: wyświetlenie strony (*page*) lub wykonanie czynności (*action*).
Nazwa kontrolera mapuje się na ścieżkę zapytania HTTP,
a rodzaj mapuje się na metodę zapytania HTTP: page - `GET`, action - `POST`.
Można myśleć o kontrolerze jak o szczególnym przypadku handlera dla interfejsu `Server`,
ale nie jest on handlerem, ponieważ nie ma dostępu do obiektu `Request` ani `Response`.
Zamiast tego otrzymuje tylko
interesujące go dane wyciągnięte z `Request` wewnątrz klasy `RequestContext`
i konstruuje obiekt `Response` pośrednio za pomocą interfejsu `Responses`.
Kontrolery nie wiedzą, że odpowiedź jest konkretnie typu `forum.web.Response`,
ponieważ są polimorficzne po typie odpowiedzi.
Opisują więc czynności handlerów w bardziej "abstrakcyjny" sposób niż same handlery,
bez przejmowania się o takie szczegóły jak wykorzystywanie menedżera sesji
czy zwracanie odpowiedniego statusu odpowiedzi.
W ten sposób pakiet `forum.controllers` w ogóle nie zależy od pakietu `forum.web`.
Wszystkie kontrolery w tym pakiecie są testowane.

Pakiet `forum.controllers.decorators` zawiera różne dekoratory kontrolerów,
które wykonują często powtarzające się czynności takie jak wymaganie
obecności parametru zapytania czy wymaganie zalogowania użytkownika.
Klasa `RequestContext` pełni również funkcję schowka na różne dane,
które dekoratory przekazują właściwemu kontrolerowi,
na przykład `LoginDecorator` zapisuje tutaj tożsamość zalogowanego użytkownika.

Ponieważ jest dość duża liczba kontrolerów,
nie ma dla nich bezpośrednio napisanych oddzielnych klas,
lecz są one generowane na podstawie metod.
Metody te są tematycznie podzielone na cztery klasy:
`UserControllers`, `CommunityControllers`, `PostControllers` i `CommentControllers`.
Pomocnicza klasa `ControllerMethod`, na podstawie nazwy i adnotacji każdej z tych metod,
"zamienia" ją na obiekt `Controller` opakowany w odpowiednie dekoratory.
Użycie refleksji jest tutaj jedynie ułatwieniem syntaktycznym,
które można zastąpić przez ręczną konstrukcję kontrolera wykonującego daną metodę
i ręcznę opakowanie go w odpowiednie dekoratory.
Na przykład wywołanie
```java
ControllerMethod.<Response>build(UserControllers.class, "updateEmail")
```
można zastąpić przez
```java
ParameterDecorator.decorate(LoginDecorator.decorate(new Controller<Response>() {
    @Override
    public Type type() { return Type.Action; }

    @Override
    public String path() { return "updateEmail"; }

    @Override
    public Response control(Forum forum, RequestContext request, Responses<Response> responses) {
        return new UserControllers<>(forum, request, responses).updateEmail();
    }
}), "email")
```

---

Klasa `ForumApp` w pakiecie `forum` łączy ze sobą te wszystkie rzeczy -
przyjmuje jako zależności obiekt `Forum` oraz implementacje interfejsów z pakietu `forum.web`,
tworzy wszystkie kontrolery z pakietu `forum.controllers`,
na ich podstawie tworzy handlery, które przekazuje serwerowi i uruchamia go.
Klasa `ForumAppFactory` konstruuje `Forum` i `ForumApp`,
wstrzykując do nich odpowiednie zależności.

---

Pakiet `impl` zawiera implementacje wszystkich interfejsów:

- `Database` - [SQLite](https://sqlite.org/index.html)
- `Server` - [Jetty](https://www.eclipse.org/jetty/)
- `TemplateProcessor` - [FreeMarker](https://freemarker.apache.org/)
- `SessionManager` - prosta implementacja z `HashMap`
- `Clock` - zegar systemowy
- `Authenticator` - hashowanie algorytmem SHA-1
- `Validator` - proste sprawdzanie regexów
- `Emailer` - sztuczna implementacja

Klasa `Program` zawiera funkcję `main`,
która uruchamia `ForumApp` z tymi implementacjami.
