package API.KVServer;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {    // класс для взаимодействия с KV-сервером
    private HttpClient client = HttpClient.newHttpClient();
    private String API_KEY;

    public KVTaskClient() throws IOException {    // конструктор с регистрацией для получения ключа
        URI uri = URI.create("http://localhost:8078/register");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            API_KEY = response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Something went wrong");
        }
    }

    public KVTaskClient(String API_KEY) {    // конструктор для восстановления по старому ключу (без регистрации)
        this.API_KEY = API_KEY;
    }

    public void put(String API_KEY, String json, String key) {    // сохранение осстояния по ключу
        URI uri = URI.create(String.format("http://localhost:8078/save/%s?API_KEY=", key) + API_KEY);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        try {
            client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String load(String API_KEY, String key) {    // загрузка осстояния по ключу
        URI uri = URI.create(String.format("http://localhost:8078/load/%s?API_KEY=", key) + API_KEY);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAPI_KEY() {    // геттер для ключа
        return API_KEY;
    }
}