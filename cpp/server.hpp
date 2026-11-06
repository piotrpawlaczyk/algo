// Gotowa obsluga HTTP. Nie musisz tego czytac ani zmieniac.
#pragma once

#include <functional>
#include <map>
#include <string>
#include <vector>

#include "nlohmann/json.hpp"

using json = nlohmann::json;

// Parametry ze sciezki, np. dla "/tasks/{id}" i adresu /tasks/7: {"id": "7"}.
using Params = std::map<std::string, std::string>;

// Odpowiedz endpointu: kod statusu + dane (null = brak tresci).
struct Response {
    int status;
    json body;
};

// Endpoint: metoda HTTP + sciezka (moze zawierac {parametry}) + funkcja obslugujaca.
struct Route {
    std::string method;
    std::string path;
    std::function<Response(const json& body, const Params& params)> handler;
};

// Zwraca body[name], jesli to napis; w przeciwnym razie pusty napis "".
inline std::string get_string(const json& body, const std::string& name) {
    if (body.is_object() && body.contains(name) && body[name].is_string()) {
        return body[name].get<std::string>();
    }
    return "";
}

// Uruchamia serwer (blokuje do zatrzymania). Najpierw sprawdza is_valid_title.
void run_server(const std::vector<Route>& routes, std::function<bool(const std::string&)> is_valid_title);
