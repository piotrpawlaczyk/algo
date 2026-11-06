"""Gotowa obsługa HTTP. Nie musisz tego czytać ani zmieniać."""

import json
import os
import traceback
import socket
import threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse


def get_string(body, name):
    """Zwraca body[name], jeśli to napis; w przeciwnym razie pusty napis ""."""
    value = body.get(name) if isinstance(body, dict) else None
    return value if isinstance(value, str) else ""


# (opis, wartość, co ma wyjść) - leci przy starcie serwera
SELF_CHECK = [
    ("Kupic mleko", "Kupic mleko", True),
    ("pusty napis", "", False),
    ("3 spacje", "   ", False),
    ("spacja, tab, enter, spacja", " \t\n ", False),
    ("100 znakow", "a" * 100, True),
    ("101 znakow", "a" * 101, False),
    ("100 znakow + spacje na brzegach", "   " + "a" * 100 + "   ", True),
]


def _self_check(is_valid_title):
    print("[walidacja] Sprawdzam is_valid_title (Czesc 1):")
    passed = 0
    for label, value, expected in SELF_CHECK:
        try:
            result = is_valid_title(value)
            ok = bool(result) == expected
            info = f"-> {result}" + ("" if ok else f" (powinno byc {expected})")
        except Exception as e:  # noqa: BLE001
            ok = False
            info = f"-> wyjatek {type(e).__name__}: {e}"
        passed += ok
        print(f"[walidacja]   {'OK  ' if ok else 'ZLE '}  {label:34} {info}")
    print(f"[walidacja] wynik: {passed}/{len(SELF_CHECK)}\n")


def _match(pattern, path):
    """Dopasowuje ścieżkę do wzorca z {parametrami}. Zwraca słownik parametrów albo None."""
    p_parts, parts = pattern.strip("/").split("/"), path.strip("/").split("/")
    if len(p_parts) != len(parts):
        return None
    params = {}
    for p, part in zip(p_parts, parts):
        if p.startswith("{") and p.endswith("}") and part:
            params[p[1:-1]] = part
        elif p != part:
            return None
    return params


_handler_lock = threading.Lock()  # endpointy wykonują się po kolei -> dane bez wyścigów


class _Server(ThreadingHTTPServer):
    # Wątki, bo przeglądarka trzyma otwarte "zapasowe" połączenia - jeden wątek by na nich utknął.
    daemon_threads = True
    # Na Windowsie SO_REUSEADDR pozwala dwóm serwerom zająć ten sam port - chcemy czytelny błąd.
    allow_reuse_address = os.name != "nt"

    def server_bind(self):
        if os.name == "nt" and hasattr(socket, "SO_EXCLUSIVEADDRUSE"):
            self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_EXCLUSIVEADDRUSE, 1)
        super().server_bind()


def _make_handler(routes):
    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            self._dispatch("GET")

        def do_POST(self):
            self._dispatch("POST")

        def do_PUT(self):
            self._dispatch("PUT")

        def do_PATCH(self):
            self._dispatch("PATCH")

        def do_DELETE(self):
            self._dispatch("DELETE")

        def _dispatch(self, method):
            path = urlparse(self.path).path
            body = self._read_body()
            status, data = 404, {"error": "Not found"}
            for route_method, pattern, func in routes:
                params = _match(pattern, path)
                if params is None:
                    continue
                if route_method != method:
                    status, data = 405, {"error": "Method not allowed"}
                    continue
                try:
                    with _handler_lock:
                        status, data = func(body, params)
                except Exception:  # noqa: BLE001
                    traceback.print_exc()
                    status, data = 500, {"error": "Blad w kodzie endpointu - szczegoly w konsoli serwera"}
                break
            self._send(status, data)

        def _read_body(self):
            length = int(self.headers.get("Content-Length") or 0)
            if length <= 0:
                return {}
            try:
                data = json.loads(self.rfile.read(length).decode("utf-8"))
            except ValueError:
                return {}
            return data if isinstance(data, dict) else {}

        def _send(self, status, data):
            try:
                payload = b"" if data is None else json.dumps(data, ensure_ascii=False).encode("utf-8")
            except TypeError as e:
                print(f"[blad] Nie da sie zamienic odpowiedzi na JSON: {e}")
                status, payload = 500, b'{"error": "Odpowiedzi nie da sie zamienic na JSON"}'
            self.send_response(status)
            if data is not None:
                self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)

    return Handler


def run(routes, is_valid_title=None, host="127.0.0.1"):
    port = int(os.environ.get("PORT", "8080"))
    if is_valid_title is not None:
        _self_check(is_valid_title)
    try:
        httpd = _Server((host, port), _make_handler(routes))
    except OSError as e:
        print(f"Nie udalo sie uruchomic serwera na porcie {port} ({e}).")
        print("Najczesciej oznacza to, ze poprzedni serwer nadal dziala - zamknij go (Ctrl+C) i sprobuj ponownie.")
        return
    print(f"Serwer dziala: http://{host}:{port}/health   (Ctrl+C, aby zatrzymac)", flush=True)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        httpd.server_close()
