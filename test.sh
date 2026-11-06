#!/usr/bin/env bash
# Szybki smoke test Twojego serwera (Czesc 2). Odpal przy dzialajacym serwerze:
#     bash test.sh
# Inny port:  PORT=8081 bash test.sh
#
# To tylko pomoc, a nie ocena. Skrypt nie rusza Twojego kodu, tylko wysyla requesty.
# (I nie, to nie jest pentest - spokojnie ;) )

BASE="http://127.0.0.1:${PORT:-8080}"
PASSED=0
TOTAL=0
STAMP=$(date +%H%M%S)

# request METODA SCIEZKA [BODY]  -> wrzuca wynik do $CODE i $BODY
request() {
    local tmp
    tmp=$(mktemp)
    if [ -n "$3" ]; then
        CODE=$(curl -s -o "$tmp" -w "%{http_code}" -X "$1" "$BASE$2" -H "Content-Type: application/json" -d "$3")
    else
        CODE=$(curl -s -o "$tmp" -w "%{http_code}" -X "$1" "$BASE$2")
    fi
    BODY=$(cat "$tmp")
    rm -f "$tmp"
}

# check OPIS CZY_OK(0/1) - wypisuje OK/ZLE i liczy punkty
check() {
    TOTAL=$((TOTAL + 1))
    if [ "$2" = "1" ]; then
        PASSED=$((PASSED + 1))
        printf "OK    %-52s %s %s\n" "$1" "$CODE" "$BODY"
    else
        printf "ZLE   %-52s %s %s\n" "$1" "$CODE" "$BODY"
    fi
}

is() { [ "$1" = "$2" ] && echo 1 || echo 0; }

request GET /health
if [ "$CODE" = "000" ]; then
    echo "Serwer nie odpowiada pod $BASE - na pewno go odpaliles/odpalilas?"
    exit 1
fi
check "GET /health -> 200" "$(is "$CODE" 200)"

request GET /tasks
check "GET /tasks -> 200 i lista [...]" "$( [ "$CODE" = 200 ] && [ "${BODY:0:1}" = "[" ] && echo 1 || echo 0)"

request POST /tasks "{\"title\": \"Test A $STAMP\"}"
ID_A=$(printf '%s' "$BODY" | sed -n 's/.*"id" *: *\([0-9][0-9]*\).*/\1/p')
check "POST poprawny tytul -> 201 z id" "$( [ "$CODE" = 201 ] && [ -n "$ID_A" ] && echo 1 || echo 0)"

request POST /tasks "{\"title\": \"Test B $STAMP\"}"
ID_B=$(printf '%s' "$BODY" | sed -n 's/.*"id" *: *\([0-9][0-9]*\).*/\1/p')
check "POST drugi tytul -> 201 i inne id" "$( [ "$CODE" = 201 ] && [ -n "$ID_B" ] && [ "$ID_A" != "$ID_B" ] && echo 1 || echo 0)"

request POST /tasks '{"title": "   "}'
check "POST same spacje -> 400" "$(is "$CODE" 400)"

request POST /tasks '{}'
check "POST bez tytulu -> 400" "$(is "$CODE" 400)"

request GET /tasks
OK=0
case "$BODY" in
    *"Test A $STAMP"*"Test B $STAMP"*) OK=1 ;;
esac
if printf '%s' "$BODY" | grep -q '"title" *: *" *"'; then OK=0; fi
check "GET /tasks -> sa oba nowe zadania, nie ma pustych" "$OK"

echo
echo "Wynik: $PASSED/$TOTAL"
