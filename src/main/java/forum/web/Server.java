package forum.web;

// interfejs do serwera;
// przed uruchomieniem należy przekazać handlery, czyli funkcje generujące odpowiedź z zapytania;
// teoretycznie wystarcza przekazanie tylko domyślnego handlera, który sam mapuje ścieżki do innych handlerów,
// ale lepiej uznać to mapowanie za odpowiedzialność serwera
public interface Server {
    void addHandler(Request.Method method, String path, Handler handler);
    void run(Handler defaultHandler);

    interface Handler {
        Response handle(Request request);
    }
}
