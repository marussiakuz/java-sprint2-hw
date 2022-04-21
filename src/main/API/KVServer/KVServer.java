package API.KVServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class KVServer {    // сервер для сохранения и восстановления состояния менеджера

    public static final int PORT = 8078;
    private final String API_KEY;
    private HttpServer server;
    private Map<String, String> data = new HashMap<>();

    public KVServer() throws IOException {
        API_KEY = generateApiKey();
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.createContext("/register", (h) -> {
            try {
                switch (h.getRequestMethod()) {
                    case "GET":
                        sendText(h, API_KEY);
                        System.out.println("API_KEY: " + API_KEY);
                        break;
                    default:
                        System.out.println("/register is waiting for a GET-request, but received " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
        server.createContext("/save", (h) -> {
            try {
                if (!hasAuth(h)) {
                    System.out.println("The request is not authorized, need a parameter in query API_KEY with the value" +
                            " API_KEY");
                    h.sendResponseHeaders(403, 0);
                    return;
                }
                switch (h.getRequestMethod()) {
                    case "POST":
                        String key = h.getRequestURI().getPath().substring("/save/".length());
                        if (key.isEmpty()) {
                            System.out.println("The key to save is empty. The key is specified in the path: " +
                                    "/save/{key}");
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        String value = readText(h);
                        if (value.isEmpty()) {
                            System.out.println("The value to save is empty. The value is specified in the request body");
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        data.put(key, value);
                        h.sendResponseHeaders(200, 0);
                        break;
                    default:
                        System.out.println("/save is waiting for a POST-request, but received: " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
        server.createContext("/load", (h) -> {
            try {
                if (!hasAuth(h)) {
                    System.out.println("The request is not authorized, need a parameter in query API_KEY with the value"
                            + " API_KEY");
                    h.sendResponseHeaders(403, 0);
                    return;
                }
                switch (h.getRequestMethod()) {
                    case "GET":
                        String key = h.getRequestURI().getPath().substring("/load/".length());
                        if (key.isEmpty()) {
                            System.out.println("The key to save is empty. The key is specified in the path: /load/{key}");
                            h.sendResponseHeaders(400, 0);
                            return;
                        }
                        if (!data.containsKey(key)) {
                            System.out.println("Key not found");
                            h.sendResponseHeaders(404, 0);
                            return;
                        }
                        String text = data.get(key);
                        sendText(h, text);
                        break;
                    default:
                        System.out.println("/load is waiting for a GET-request, but received: " + h.getRequestMethod());
                        h.sendResponseHeaders(405, 0);
                }
            } finally {
                h.close();
            }
        });
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private String generateApiKey() {
        return "" + System.currentTimeMillis();
    }

    protected boolean hasAuth(HttpExchange h) {
        String rawQuery = h.getRequestURI().getRawQuery();
        return rawQuery != null && (rawQuery.contains("API_KEY=" + API_KEY) || rawQuery.contains("API_KEY=DEBUG"));
    }

    protected String readText(HttpExchange h) throws IOException {
        return new String(h.getRequestBody().readAllBytes(), "UTF-8");
    }

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes("UTF-8");
        h.getResponseHeaders().add("Content-Type", "application/json");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
    }
}
