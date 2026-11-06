// Gotowa obsluga HTTP (cpp-httplib). Nie musisz tego czytac ani zmieniac.

// cpp-httplib wymaga Windows 10+; starsze MinGW domyslnie ustawiaja nizsza wersje.
#if defined(_WIN32) && !defined(_WIN32_WINNT)
#define _WIN32_WINNT 0x0A00
#endif

#include "server.hpp"

#include <cstdio>
#include <cstdlib>
#include <exception>
#include <iostream>
#include <mutex>
#include <sstream>

#include "httplib.h"

namespace {

std::vector<std::string> split_path(const std::string& path) {
    std::vector<std::string> parts;
    std::stringstream ss(path);
    std::string part;
    while (std::getline(ss, part, '/')) {
        if (!part.empty()) parts.push_back(part);
    }
    return parts;
}

bool match(const std::string& pattern, const std::string& path, Params& params) {
    std::vector<std::string> p = split_path(pattern), a = split_path(path);
    if (p.size() != a.size()) return false;
    params.clear();
    for (std::size_t i = 0; i < p.size(); ++i) {
        if (p[i].size() > 2 && p[i].front() == '{' && p[i].back() == '}') {
            params[p[i].substr(1, p[i].size() - 2)] = a[i];
        } else if (p[i] != a[i]) {
            return false;
        }
    }
    return true;
}

void self_check(const std::function<bool(const std::string&)>& is_valid_title) {
    struct Case { const char* label; std::string value; bool expected; };
    const std::vector<Case> cases = {
        {"Kupic mleko", "Kupic mleko", true},
        {"pusty napis", "", false},
        {"3 spacje", "   ", false},
        {"spacja, tab, enter, spacja", " \t\n ", false},
        {"100 znakow", std::string(100, 'a'), true},
        {"101 znakow", std::string(101, 'a'), false},
        {"100 znakow + spacje na brzegach", "   " + std::string(100, 'a') + "   ", true},
    };
    std::cout << "[walidacja] Sprawdzam is_valid_title (Czesc 1):\n";
    int passed = 0;
    for (const Case& c : cases) {
        bool ok = false;
        std::string info;
        try {
            bool result = is_valid_title(c.value);
            ok = result == c.expected;
            info = std::string("-> ") + (result ? "true" : "false") +
                   (ok ? "" : (c.expected ? " (powinno byc true)" : " (powinno byc false)"));
        } catch (const std::exception& e) {
            info = std::string("-> wyjatek: ") + e.what();
        } catch (...) {
            info = "-> nieznany wyjatek";
        }
        if (ok) passed++;
        char line[96];
        std::snprintf(line, sizeof line, "[walidacja]   %s  %-34s ", ok ? "OK  " : "ZLE ", c.label);
        std::cout << line << info << "\n";
    }
    std::cout << "[walidacja] wynik: " << passed << "/" << cases.size() << "\n" << std::endl;
}

}  // namespace

void run_server(const std::vector<Route>& routes, std::function<bool(const std::string&)> is_valid_title) {
    if (is_valid_title) self_check(is_valid_title);

    const char* env_port = std::getenv("PORT");
    int port = env_port ? std::atoi(env_port) : 8080;

    httplib::Server server;
    // Kilka watkow przyjmuje polaczenia (przegladarka trzyma otwarte "zapasowe" polaczenia,
    // jeden watek by na nich utykal), ale endpointy wykonuja sie po kolei - patrz handler_mutex.
    server.new_task_queue = [] { return new httplib::ThreadPool(8); };
    // Drugi uruchomiony serwer ma dostac blad "port zajety", a nie po cichu dzielic port z pierwszym.
    server.set_socket_options([](socket_t sock) {
#ifdef _WIN32
        httplib::set_socket_opt(sock, SOL_SOCKET, SO_EXCLUSIVEADDRUSE, 1);
#else
        httplib::set_socket_opt(sock, SOL_SOCKET, SO_REUSEADDR, 1);
#endif
    });

    static std::mutex handler_mutex;
    auto handle = [&routes](const httplib::Request& req, httplib::Response& res) {
        std::lock_guard<std::mutex> lock(handler_mutex);  // jeden request naraz -> dane bez wyscigow
        Response response{404, json{{"error", "Not found"}}};
        try {
            json body = json::object();
            if (!req.body.empty()) {
                json parsed = json::parse(req.body, nullptr, /*allow_exceptions=*/false);
                if (parsed.is_object()) body = parsed;
            }
            for (const Route& route : routes) {
                Params params;
                if (!match(route.path, req.path, params)) continue;
                if (route.method != req.method) {
                    response = Response{405, json{{"error", "Method not allowed"}}};
                    continue;
                }
                response = route.handler(body, params);
                break;
            }
            res.status = response.status;
            if (!response.body.is_null()) {
                res.set_content(response.body.dump(), "application/json; charset=utf-8");
            }
        } catch (const std::exception& e) {
            std::cerr << "[blad] " << e.what() << std::endl;
            res.status = 500;
            res.set_content(json{{"error", "Blad w kodzie endpointu - szczegoly w konsoli serwera"}}.dump(),
                            "application/json; charset=utf-8");
        }
        std::cout << req.method << " " << req.path << " -> " << res.status << std::endl;
    };

    server.Get(".*", handle);
    server.Post(".*", handle);
    server.Put(".*", handle);
    server.Patch(".*", handle);
    server.Delete(".*", handle);

    if (!server.bind_to_port("127.0.0.1", port)) {
        std::cout << "Nie udalo sie uruchomic serwera na porcie " << port << ".\n"
                  << "Najczesciej oznacza to, ze poprzedni serwer nadal dziala - zamknij go (Ctrl+C) i sprobuj ponownie."
                  << std::endl;
        return;
    }
    std::cout << "Serwer dziala: http://127.0.0.1:" << port << "/health   (Ctrl+C, aby zatrzymac)" << std::endl;
    server.listen_after_bind();
}
