// Zadanie rekrutacyjne - Backend (C++)
//
// Jak odpalic: zajrzyj do README.md w tym folderze.
// Serwer wstaje na http://127.0.0.1:8080, ubijasz go przez Ctrl+C.
//
// Grzebiesz TYLKO w tym pliku. server.hpp / server.cpp to gotowa obsluga HTTP,
// mozesz je olac. JSON-em zajmuje sie biblioteka nlohmann/json (typ `json`).

#include <string>
#include <vector>

#include "server.hpp"

// ============================================================================
//  CZESC 1: walidacja
// ============================================================================

const std::size_t MAX_TITLE_LENGTH = 100;

// Sprawdza, czy tytul zadania jest ok.
//
// Najpierw obcinasz biale znaki (spacje, taby, entery) z poczatku i konca.
// Potem tytul jest poprawny, jesli:
//   - NIE jest pusty,
//   - ma maksymalnie MAX_TITLE_LENGTH znakow.
//
// Przyklady:
//   "Kupic mleko"                     -> true
//   ""                                -> false
//   "   "                             -> false
//   std::string(101, 'a')             -> false
//   "  " + std::string(100, 'a')      -> true   (spacje na brzegach sie nie licza)
//
// Jak odpalisz serwer, to sam przetestuje te funkcje i wypluje wynik w konsoli.
bool is_valid_title(const std::string& title) {
    // TODO (Czesc 1): na razie ta funkcja przepuszcza wszystko jak dziurawy firewall.
    return true;
}

// ============================================================================
//  Dane - trzymamy je w RAM-ie, po restarcie serwera wszystko znika
// ============================================================================

std::vector<json> tasks;  // lista zadan; kazde zadanie to obiekt JSON, np. {"id": 1, "title": "Kupic mleko"}
long long next_id = 1;    // id, ktore dostanie nastepne zadanie

// ============================================================================
//  CZESC 2: endpointy
//
//  Kazdy endpoint dostaje:
//    body   - tresc requesta jako obiekt JSON (pusty {}, jak jej nie ma albo to nie JSON)
//    params - parametry ze sciezki, np. dla "/tasks/{id}" i adresu /tasks/7: params["id"] == "7"
//  i zwraca Response{kod_statusu, dane}, np. Response{200, json{{"status", "ok"}}}.
//  Odpowiedz bez tresci: Response{204, nullptr}.
//
//  Sciaga z nlohmann/json:
//    json obj = {{"id", 1}, {"title", "abc"}};   // obiekt {"id": 1, "title": "abc"}
//    json arr = json::array();  arr.push_back(obj); // tablica [ ... ]
// ============================================================================

// GET /health - przykladowy endpoint, ktory juz dziala. Mozesz podejrzec, jak to wyglada.
Response health(const json& body, const Params& params) {
    return Response{200, json{{"status", "ok"}}};
}

// GET /tasks - zwraca kod 200 i liste wszystkich zadan, np.
//   [{"id": 1, "title": "Kupic mleko"}, {"id": 2, "title": "Nauczyc sie Gita"}]
Response list_tasks(const json& body, const Params& params) {
    // TODO (Czesc 2)
    return Response{501, json{{"error", "Not implemented"}}};
}

// POST /tasks - tworzy zadanie, np. dla body {"title": "Kupic mleko"}.
//
//   - tytul ok     -> kod 201 i nowe zadanie, np. {"id": 1, "title": "Kupic mleko"}
//                     (kazde zadanie musi miec inne id)
//   - tytul nie ok -> kod 400 i {"error": "<opis>"} - i NICZEGO nie zapisujemy
//
// Hint: get_string(body, "title") zwraca tytul albo "", jak go nie ma
// albo to nie jest string.
Response create_task(const json& body, const Params& params) {
    // TODO (Czesc 2)
    return Response{501, json{{"error", "Not implemented"}}};
}

// ============================================================================
//  Lista endpointow: {metoda, sciezka, funkcja}
// ============================================================================

int main() {
    std::vector<Route> routes = {
        {"GET", "/health", health},
        {"GET", "/tasks", list_tasks},
        {"POST", "/tasks", create_task},
    };
    run_server(routes, is_valid_title);
    return 0;
}
