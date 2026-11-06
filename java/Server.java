import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/*
 * Gotowa obsluga HTTP. Nie musisz tego czytac ani zmieniac.
 *
 * Co Ci sie przyda:
 *   new Response(status, dane)       - odpowiedz endpointu
 *   Json.obj("klucz", wartosc, ...)  - tworzy mape (obiekt JSON) z par klucz-wartosc
 *   Json.getString(body, "nazwa")    - odczytuje pole tekstowe z body ("" gdy go brak)
 */

/** Odpowiedz endpointu: kod statusu + dane (mapa, lista, napis, liczba albo null = brak tresci). */
final class Response {
    final int status;
    final Object body;

    Response(int status, Object body) {
        this.status = status;
        this.body = body;
    }
}

/** Endpoint: metoda HTTP + sciezka (moze zawierac {parametry}) + metoda obslugujaca. */
final class Route {
    interface Handler {
        Response handle(Map<String, Object> body, Map<String, String> params);
    }

    final String method;
    final String path;
    final Handler handler;

    Route(String method, String path, Handler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;
    }
}

/** Mini-parser JSON-a, zeby nie ciagnac zadnych bibliotek. Napisany recznie, wystarczy do tego zadania. */
final class Json {
    private Json() {}

    /** Json.obj("id", 1, "title", "abc") -> mapa {"id": 1, "title": "abc"} (kolejnosc zachowana). */
    static Map<String, Object> obj(Object... keysAndValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keysAndValues.length; i += 2) {
            map.put(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
        }
        return map;
    }

    /** Zwraca body.get(name), jesli to napis; w przeciwnym razie pusty napis "". */
    static String getString(Map<String, Object> body, String name) {
        Object value = body == null ? null : body.get(name);
        return value instanceof String ? (String) value : "";
    }

    static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object v, StringBuilder out) {
        if (v == null) {
            out.append("null");
        } else if (v instanceof String) {
            writeString((String) v, out);
        } else if (v instanceof Number || v instanceof Boolean) {
            out.append(v);
        } else if (v instanceof Map) {
            out.append('{');
            String sep = "";
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                out.append(sep);
                sep = ",";
                writeString(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
        } else if (v instanceof Iterable) {
            out.append('[');
            String sep = "";
            for (Object item : (Iterable<?>) v) {
                out.append(sep);
                sep = ",";
                write(item, out);
            }
            out.append(']');
        } else {
            throw new IllegalArgumentException("Nie umiem zamienic na JSON: " + v.getClass().getSimpleName());
        }
    }

    private static void writeString(String s, StringBuilder out) {
        out.append('"');
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\') out.append('\\').append(c);
            else if (c == '\n') out.append("\\n");
            else if (c == '\r') out.append("\\r");
            else if (c == '\t') out.append("\\t");
            else if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
            else out.append(c);
        }
        out.append('"');
    }

    /** Parsuje tekst JSON. Rzuca IllegalArgumentException przy blednym JSON-ie. */
    static Object parse(String text) {
        Parser p = new Parser(text);
        Object v = p.value();
        p.ws();
        if (p.pos != text.length()) throw new IllegalArgumentException("nadmiarowe znaki");
        return v;
    }

    private static final class Parser {
        final String s;
        int pos = 0;

        Parser(String s) {
            this.s = s;
        }

        void ws() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }

        char next() {
            if (pos >= s.length()) throw new IllegalArgumentException("nieoczekiwany koniec");
            return s.charAt(pos++);
        }

        void expect(char c) {
            ws();
            if (next() != c) throw new IllegalArgumentException("oczekiwano " + c);
        }

        Object value() {
            ws();
            if (pos >= s.length()) throw new IllegalArgumentException("nieoczekiwany koniec");
            char c = s.charAt(pos);
            if (c == '{') {
                pos++;
                Map<String, Object> map = new LinkedHashMap<>();
                ws();
                if (pos < s.length() && s.charAt(pos) == '}') { pos++; return map; }
                do {
                    ws();
                    if (pos >= s.length() || s.charAt(pos) != '"') throw new IllegalArgumentException("oczekiwano klucza");
                    String key = string();
                    expect(':');
                    map.put(key, value());
                    ws();
                } while (pos < s.length() && s.charAt(pos) == ',' && ++pos > 0);
                expect('}');
                return map;
            }
            if (c == '[') {
                pos++;
                List<Object> list = new ArrayList<>();
                ws();
                if (pos < s.length() && s.charAt(pos) == ']') { pos++; return list; }
                do {
                    list.add(value());
                    ws();
                } while (pos < s.length() && s.charAt(pos) == ',' && ++pos > 0);
                expect(']');
                return list;
            }
            if (c == '"') return string();
            for (String word : new String[] {"true", "false", "null"}) {
                if (s.startsWith(word, pos)) {
                    pos += word.length();
                    return word.equals("null") ? null : Boolean.valueOf(word);
                }
            }
            int start = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) pos++;
            String num = s.substring(start, pos);
            try {
                return num.matches("-?\\d+") ? (Object) Long.parseLong(num) : (Object) Double.parseDouble(num);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("bledna wartosc");
            }
        }

        String string() {
            pos++; // otwierajacy cudzyslow
            StringBuilder sb = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') return sb.toString();
                if (c != '\\') { sb.append(c); continue; }
                char e = next();
                switch (e) {
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'u':
                        if (pos + 4 > s.length()) throw new IllegalArgumentException("bledne \\u");
                        sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                        pos += 4;
                        break;
                    default: sb.append(e);
                }
            }
        }
    }
}

public final class Server {
    private Server() {}

    private static final Object[][] SELF_CHECK = {
        {"Kupic mleko", "Kupic mleko", true},
        {"pusty napis", "", false},
        {"3 spacje", "   ", false},
        {"spacja, tab, enter, spacja", " \t\n ", false},
        {"100 znakow", "a".repeat(100), true},
        {"101 znakow", "a".repeat(101), false},
        {"100 znakow + spacje na brzegach", "   " + "a".repeat(100) + "   ", true},
    };

    static void start(List<Route> routes, Predicate<String> isValidTitle) throws IOException {
        if (isValidTitle != null) selfCheck(isValidTitle);
        String env = System.getenv("PORT");
        int port = env == null ? 8080 : Integer.parseInt(env);
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        } catch (BindException e) {
            System.out.println("Nie udalo sie uruchomic serwera na porcie " + port + " (" + e.getMessage() + ").");
            System.out.println("Najczesciej oznacza to, ze poprzedni serwer nadal dziala - zamknij go (Ctrl+C) i sprobuj ponownie.");
            return;
        }
        server.createContext("/", exchange -> handle(exchange, routes));
        server.setExecutor(null); // zadania obslugiwane po kolei, jednym watkiem
        server.start();
        System.out.println("Serwer dziala: http://127.0.0.1:" + port + "/health   (Ctrl+C, aby zatrzymac)");
    }

    private static void selfCheck(Predicate<String> isValidTitle) {
        System.out.println("[walidacja] Sprawdzam isValidTitle (Czesc 1):");
        int passed = 0;
        for (Object[] c : SELF_CHECK) {
            boolean expected = (Boolean) c[2];
            boolean ok;
            String info;
            try {
                boolean result = isValidTitle.test((String) c[1]);
                ok = result == expected;
                info = "-> " + result + (ok ? "" : " (powinno byc " + expected + ")");
            } catch (RuntimeException e) {
                ok = false;
                info = "-> wyjatek " + e;
            }
            if (ok) passed++;
            System.out.println(String.format("[walidacja]   %s  %-34s %s", ok ? "OK  " : "ZLE ", c[0], info));
        }
        System.out.println("[walidacja] wynik: " + passed + "/" + SELF_CHECK.length);
        System.out.println();
    }

    private static Map<String, String> match(String pattern, String path) {
        String[] p = pattern.replaceAll("^/+|/+$", "").split("/", -1);
        String[] a = path.replaceAll("^/+|/+$", "").split("/", -1);
        if (p.length != a.length) return null;
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < p.length; i++) {
            if (p[i].startsWith("{") && p[i].endsWith("}") && !a[i].isEmpty()) {
                params.put(p[i].substring(1, p[i].length() - 1), a[i]);
            } else if (!p[i].equals(a[i])) {
                return null;
            }
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private static void handle(HttpExchange ex, List<Route> routes) throws IOException {
        Response response = new Response(404, Json.obj("error", "Not found"));
        try {
            String method = ex.getRequestMethod().toUpperCase();
            String path = ex.getRequestURI().getPath();
            byte[] raw = ex.getRequestBody().readAllBytes();
            Map<String, Object> body = new LinkedHashMap<>();
            try {
                Object parsed = Json.parse(new String(raw, StandardCharsets.UTF_8));
                if (parsed instanceof Map) body = (Map<String, Object>) parsed;
            } catch (IllegalArgumentException ignored) {
                // brak tresci albo niepoprawny JSON -> pusta mapa
            }
            for (Route route : routes) {
                Map<String, String> params = match(route.path, path);
                if (params == null) continue;
                if (!route.method.equals(method)) {
                    response = new Response(405, Json.obj("error", "Method not allowed"));
                    continue;
                }
                response = route.handler.handle(body, params);
                if (response == null) throw new IllegalStateException("endpoint zwrocil null zamiast Response");
                break;
            }
            send(ex, response);
            System.out.println(method + " " + path + " -> " + response.status);
        } catch (Exception e) {
            e.printStackTrace();
            send(ex, new Response(500, Json.obj("error", "Blad w kodzie endpointu - szczegoly w konsoli serwera")));
        } finally {
            ex.close();
        }
    }

    private static void send(HttpExchange ex, Response response) throws IOException {
        if (response.body == null) {
            ex.sendResponseHeaders(response.status, -1);
            return;
        }
        byte[] payload = Json.stringify(response.body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(response.status, payload.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(payload);
        }
    }
}
