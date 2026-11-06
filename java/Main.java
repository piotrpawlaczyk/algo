import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Zadanie rekrutacyjne - Backend (Java)
 *
 * Odpalasz (bedac w folderze java/), dwie komendy:
 *     javac *.java
 *     java Main
 * Serwer wstaje na http://127.0.0.1:8080, ubijasz go przez Ctrl+C.
 *
 * Grzebiesz TYLKO w tym pliku. Server.java to gotowa obsluga HTTP, mozesz go olac.
 */
public class Main {

    // ========================================================================
    //  CZESC 1: walidacja
    // ========================================================================

    static final int MAX_TITLE_LENGTH = 100;

    /**
     * Sprawdza, czy tytul zadania jest ok.
     *
     * Najpierw obcinasz biale znaki (spacje, taby, entery) z poczatku i konca.
     * Potem tytul jest poprawny, jesli:
     *   - NIE jest pusty,
     *   - ma maksymalnie MAX_TITLE_LENGTH znakow.
     *
     * Przyklady:
     *   "Kupic mleko"              -> true
     *   ""                         -> false
     *   "   "                      -> false
     *   "a".repeat(101)            -> false
     *   "  " + "a".repeat(100)     -> true   (spacje na brzegach sie nie licza)
     *
     * Jak odpalisz serwer, to sam przetestuje te metode i wypluje wynik w konsoli.
     */
    static boolean isValidTitle(String title) {
        // TODO (Czesc 1): na razie ta metoda przepuszcza wszystko jak dziurawy firewall.
        return true;
    }

    // ========================================================================
    //  Dane - trzymamy je w RAM-ie, po restarcie serwera wszystko znika
    // ========================================================================

    /** Lista zadan; kazde zadanie to mapa, np. Json.obj("id", 1, "title", "Kupic mleko"). */
    static List<Map<String, Object>> tasks = new ArrayList<>();

    /** Id, ktore dostanie nastepne zadanie. */
    static long nextId = 1;

    // ========================================================================
    //  CZESC 2: endpointy
    //
    //  Kazdy endpoint dostaje:
    //    body   - tresc requesta jako mapa (pusta, jak jej nie ma albo to nie JSON)
    //    params - parametry ze sciezki, np. dla "/tasks/{id}" i adresu /tasks/7: {"id": "7"}
    //  i zwraca new Response(kod_statusu, dane), np. new Response(200, Json.obj("status", "ok")).
    //  Dane moga byc mapa albo lista - zamiane na JSON ogarnia Server.java.
    //  Odpowiedz bez tresci: new Response(204, null).
    // ========================================================================

    /** GET /health - przykladowy endpoint, ktory juz dziala. Mozesz podejrzec, jak to wyglada. */
    static Response health(Map<String, Object> body, Map<String, String> params) {
        return new Response(200, Json.obj("status", "ok"));
    }

    /**
     * GET /tasks - zwraca kod 200 i liste wszystkich zadan, np.
     *   [{"id": 1, "title": "Kupic mleko"}, {"id": 2, "title": "Nauczyc sie Gita"}]
     */
    static Response listTasks(Map<String, Object> body, Map<String, String> params) {
        // TODO (Czesc 2)
        return new Response(501, Json.obj("error", "Not implemented"));
    }

    /**
     * POST /tasks - tworzy zadanie, np. dla body {"title": "Kupic mleko"}.
     *
     *   - tytul ok     -> kod 201 i nowe zadanie, np. {"id": 1, "title": "Kupic mleko"}
     *                     (kazde zadanie musi miec inne id)
     *   - tytul nie ok -> kod 400 i {"error": "<opis>"} - i NICZEGO nie zapisujemy
     *
     * Hint: Json.getString(body, "title") zwraca tytul albo "", jak go nie ma
     * albo to nie jest String.
     */
    static Response createTask(Map<String, Object> body, Map<String, String> params) {
        // TODO (Czesc 2)
        return new Response(501, Json.obj("error", "Not implemented"));
    }

    // ========================================================================
    //  Lista endpointow: (metoda, sciezka, metoda obslugujaca)
    // ========================================================================

    static final List<Route> ROUTES = List.of(
            new Route("GET", "/health", Main::health),
            new Route("GET", "/tasks", Main::listTasks),
            new Route("POST", "/tasks", Main::createTask)
    );

    public static void main(String[] args) throws Exception {
        Server.start(ROUTES, Main::isValidTitle);
    }
}
