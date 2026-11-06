# Zadanie rekrutacyjne – sekcja Backend

Cześć! Mamy dla Ciebie małe API do listy zadań i chcemy zobaczyć, jak sobie z nim radzisz.

## TL;DR

1. Stwórz repozytorium na swoim GitHubie z szablonu (opcja Use this template).
2. Wybór języka: repozytorium posiada wersje w C++, Java i python. Wybierz jeden, wybór nie ma znaczenia przy ocenianiu. Pozostałe foldery języków usuń i stwórz commita. Odpal serwer. Instrukcje w README danego języka.
3. **Część 1:** dokańczasz `is_valid_title`, czyli walidację tytułu.
4. **Część 2:** dopisujesz `GET /tasks` i `POST /tasks`.
5. Odpalasz `test.sh` / `test.ps1`, a po drodze commitujesz i pushujesz.

**Czas:** **45 minut** na całość, koniec o 21:00. Liczy się to, co będzie na `main` w Twoim repo, kiedy wybije termin. Commity po terminie się nie liczą. Link do Twojego repozytorium należy umieścić w formularzu zamieszczonym w mailu po zakończeniu prac.

Serwer jest już napisany, Ty dopisujesz tylko kilka funkcji w **jednym pliku**. Nie musi wyjść wszystko. Bardziej interesuje nas **Twoje podejście do problemu i styl pracy** niż to, czy wszystko świeci na zielono. Jak coś nie działa albo nie zdążysz, zostaw swoją próbę i krótki komentarz w stylu „tu chciałem zrobić X, ale wywalało Y”. To dla nas naprawdę cenne info!

**Zasady:** Zadanie realizujesz sam/sama i **bez AI** (ChatGPT, Copilot, Cursor, podpowiedzi AI w IDE…). Dokumentacja, Google i Stack Overflow jak najbardziej są dozwolone. Na rozmowie i tak wyjdzie, czy kod jest Twój. Nie chcemy was ograniczać ale musimy was poznać. Nie stresujcie się kiedy czegoś nie wiecie, to normalne. 

## Pełna instrukcja

## 1. Repo i wybór języka

1. Zrób na swoim GitHubie **publiczne** repo z tego szablonu: **„Use this template” → „Create a new repository”**. Sklonuj je.
2. Wybierz język: `python/`, `java/` albo `cpp/`. Foldery dwóch pozostałych **wywal**.
3. Commit i push. Zwracamy uwagę na nazwy commitów, więc niech mówią, co zrobiłeś/zrobiłaś.

## 2. Odpal serwer

Pracuj w edytorze, w którym czujesz się najlepiej (VS Code, IntelliJ, PyCharm, CLion, vim, cokolwiek). Do odpalenia wystarczy terminal i komendy z README Twojego języka, żadne IDE nie jest wymagane.

Instrukcja siedzi w `README.md` w folderze Twojego języka. Jak serwer działa, to w przeglądarce pod http://127.0.0.1:8080/health zobaczysz `{"status": "ok"}`.

Coś nie chce ruszyć? **Bez paniki**, pisz do nas, a w międzyczasie klep kod dalej.

## 3. Część 1: walidacja

Na razie aplikacja przyjmuje wszystko, nawet puste tytuły albo zawierające same spacje. Klasyka: nie ufaj inputowi od usera. Funkcja, która powinna to sprawdzać, jest niedokończona. **Dopisz ją** według komentarza nad nią.

Po starcie serwer sam ją testuje i wypisuje w konsoli wynik, np. `[walidacja] wynik: 7/7`. Jak masz 7/7, to znaczy, że zadanie zostało wykonane prawidłowo. Traktuj to bardziej jako informację dla Ciebie niż dla nas.

Commit i push.

## 4. Część 2: endpointy

Należy dopisać następujące endpointy:

| Endpoint | Co robi |
|---|---|
| `GET /tasks` | zwraca **200** i listę wszystkich zadań, np. `[{"id": 1, "title": "Kupic mleko"}]` |
| `POST /tasks` | dla `{"title": "Kupic mleko"}` tworzy zadanie z **unikalnym `id`** i zwraca je z kodem **201**; jak tytuł jest niepoprawny (patrz Część 1) albo go nie ma, zwraca **400** i `{"error": "..."}` i **niczego nie zapisuje** |

Commit i push.

## Bonus (jak zostanie czas)

* `GET /tasks/{id}`: **200** i zadanie albo **404**, jak takiego nie ma,
* `DELETE /tasks/{id}`: **204** (bez treści) albo **404**.

## Jak testować

Przy odpalonym serwerze otwórz **drugi** terminal w głównym folderze repo i puść:

```bash
# Linux / macOS / Git Bash
bash test.sh
```

```powershell
# Windows PowerShell
powershell -ExecutionPolicy Bypass -File test.ps1
```

Skrypt wysyła kilka requestów do Twojego serwera i przy każdym pisze `OK` albo `ZLE`, razem z kodem i odpowiedzią. To tylko pomoc, a nie wyrocznia: sprawdza kawałek Części 2, bonusu nie rusza. Każde odpalenie dodaje zadania, więc jak chcesz pustą listę, zrestartuj serwer.

Pojedynczy request wyślesz też ręcznie:

```bash
curl -i -X POST http://127.0.0.1:8080/tasks -H "Content-Type: application/json" -d '{"title": "Kupic mleko"}'
```

```powershell
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8080/tasks -ContentType "application/json" -Body '{"title": "Kupic mleko"}'
```

`GET` sprawdzisz po prostu w przeglądarce. Postman czy coś podobnego też będzie okej.

## Oddanie

Po upewnieniu się, że wszystkie Twoje zmiany są na `main`, skopiuj link do repozytorium i wrzuć go do wysłanego w mailu formularza. 

Upewnij się, że Twoje repozytorium jest publiczne!


Powodzenia!
