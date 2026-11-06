"""Zadanie rekrutacyjne - Backend (Python)

Odpalasz (będąc w folderze python/):   python main.py
Serwer wstaje na http://127.0.0.1:8080, ubijasz go przez Ctrl+C.

Grzebiesz TYLKO w tym pliku. server.py to gotowa obsługa HTTP, możesz go olać.
"""

import server
from server import get_string

# ============================================================================
#  CZĘŚĆ 1: walidacja
# ============================================================================

MAX_TITLE_LENGTH = 100


def is_valid_title(title):
    """Sprawdza, czy tytuł zadania jest ok.

    Najpierw obcinasz białe znaki (spacje, taby, entery) z początku i końca.
    Potem tytuł jest poprawny, jeśli:
      * NIE jest pusty,
      * ma maksymalnie MAX_TITLE_LENGTH znaków.

    Przykłady:
        "Kupic mleko"       -> True
        ""                  -> False
        "   "               -> False
        "a" * 101           -> False
        "  " + "a" * 100    -> True   (spacje na brzegach się nie liczą)

    Jak odpalisz serwer, to sam przetestuje tę funkcję i wypluje wynik w konsoli.
    """
    # TODO (Część 1): na razie ta funkcja przepuszcza wszystko jak dziurawy firewall.
    return True


# ============================================================================
#  Dane - trzymamy je w RAM-ie, po restarcie serwera wszystko znika
# ============================================================================

tasks = []   # lista zadań; każde zadanie to słownik, np. {"id": 1, "title": "Kupic mleko"}
next_id = 1  # id, które dostanie następne zadanie


# ============================================================================
#  CZĘŚĆ 2: endpointy
#
#  Każdy endpoint dostaje:
#    body   - treść requesta jako słownik (pusty {}, jak jej nie ma albo to nie JSON)
#    params - parametry ze ścieżki, np. dla "/tasks/{id}" i adresu /tasks/7: {"id": "7"}
#  i zwraca parę (kod_statusu, dane), np. (200, {"status": "ok"}).
#  Dane mogą być słownikiem albo listą - zamianę na JSON ogarnia server.py.
#  Odpowiedź bez treści: (204, None).
# ============================================================================

def health(body, params):
    """GET /health - przykładowy endpoint, który już działa. Możesz podejrzeć, jak to wygląda."""
    return 200, {"status": "ok"}


def list_tasks(body, params):
    """GET /tasks - zwraca kod 200 i listę wszystkich zadań, np.
        [{"id": 1, "title": "Kupic mleko"}, {"id": 2, "title": "Nauczyc sie Gita"}]
    """
    # TODO (Część 2)
    return 501, {"error": "Not implemented"}


def create_task(body, params):
    """POST /tasks - tworzy zadanie, np. dla body {"title": "Kupic mleko"}.

    * tytuł ok     -> kod 201 i nowe zadanie, np. {"id": 1, "title": "Kupic mleko"}
                      (każde zadanie musi mieć inne id)
    * tytuł nie ok -> kod 400 i {"error": "<opis>"} - i NICZEGO nie zapisujemy

    Hint: get_string(body, "title") zwraca tytuł albo "", jak go nie ma
    albo to nie jest string. Jak zmieniasz next_id, daj na początku funkcji: global next_id
    """
    # TODO (Część 2)
    return 501, {"error": "Not implemented"}

ROUTES = [
    ("GET", "/health", health),
    ("GET", "/tasks", list_tasks),
    ("POST", "/tasks", create_task),

]

if __name__ == "__main__":
    server.run(ROUTES, is_valid_title)
