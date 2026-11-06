# Java

Potrzebujesz JDK 11+ (sprawdzisz przez `javac -version`). Nic więcej nie instalujesz: bez Mavena i bez Gradle'a.

## Odpalenie

```bash
cd java
javac *.java
java Main
```

Jak serwer działa, to w przeglądarce pod http://127.0.0.1:8080/health zobaczysz `{"status":"ok"}`.

## Gdzie piszesz kod

* `Main.java`: **tu dopisujesz swój kod**,
* `Server.java`: gotowa obsługa HTTP, nie musisz tam zaglądać.

Pisz w edytorze, który lubisz. Do odpalenia wystarczą dwie komendy powyżej. Jeśli wolisz klikać w IDE, to też zadziała.

Po każdej zmianie zatrzymaj serwer (Ctrl+C), skompiluj ponownie (`javac *.java`) i odpal go jeszcze raz.
