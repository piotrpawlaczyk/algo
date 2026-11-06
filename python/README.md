# Python

Potrzebujesz Pythona 3.8+. Niczego nie instalujesz: bez `pip install` i bez venv.

## Odpalenie

```bash
cd python
python main.py
```

Jeśli `python` nie działa, spróbuj `python3 main.py` albo (na Windowsie) `py main.py`. Zależy, jak masz to u siebie ustawione.

Jak serwer działa, to w przeglądarce pod http://127.0.0.1:8080/health zobaczysz `{"status": "ok"}`.

## Gdzie piszesz kod

* `main.py`: **tu dopisujesz swój kod**,
* `server.py`: gotowa obsługa HTTP, nie musisz tam zaglądać.

Pisz w edytorze, który lubisz. Do odpalenia wystarczy terminal i komenda powyżej.

Po każdej zmianie zatrzymaj serwer (Ctrl+C) i odpal go jeszcze raz, bo sam się nie przeładowuje.
