# C++

Potrzebujesz kompilatora z obsługą C++17. Biblioteki ([cpp-httplib](https://github.com/yhirose/cpp-httplib) i [nlohmann/json](https://github.com/nlohmann/json)) już są w `third_party/`, więc nic nie ściągasz.

## Odpalenie

**Linux / macOS / WSL:**

```bash
cd cpp
g++ -std=c++17 -Ithird_party main.cpp server.cpp -o server -pthread
./server
```

**Windows (MinGW / MSYS2):**

```bash
cd cpp
g++ -std=c++17 -Ithird_party main.cpp server.cpp -o server.exe -lws2_32 -static
./server.exe          # w PowerShell / cmd: .\server.exe
```

Jak serwer działa, to w przeglądarce pod http://127.0.0.1:8080/health zobaczysz `{"status":"ok"}`.

## Szybsza kompilacja (polecamy)

`server.cpp` zawiera sporą bibliotekę HTTP, więc pełna kompilacja trwa kilkanaście sekund albo dłużej. Nie musisz go zmieniać, więc wystarczy skompilować go **raz**, a potem kompilować już tylko `main.cpp`:

```bash
g++ -std=c++17 -Ithird_party -c server.cpp -o server.o                 # raz, to chwilę trwa
g++ -std=c++17 -Ithird_party main.cpp server.o -o server -pthread      # po każdej zmianie, kilka sekund
# Windows (MinGW): w drugiej komendzie zamiast -pthread daj -lws2_32 -static i -o server.exe
```

## Gdzie piszesz kod

* `main.cpp`: **tu dopisujesz swój kod**,
* `server.hpp`, `server.cpp`: gotowa obsługa HTTP, nie musisz tam zaglądać.

Pisz w edytorze, który lubisz. Do odpalenia wystarczą komendy `g++` powyżej. Jeśli używasz IDE z obsługą CMake (np. CLion, Visual Studio, VS Code z CMake Tools), możesz też po prostu otworzyć folder `cpp`, bo jest w nim `CMakeLists.txt`. To tylko opcja.

Po każdej zmianie zatrzymaj serwer (Ctrl+C), skompiluj ponownie i odpal go jeszcze raz.
