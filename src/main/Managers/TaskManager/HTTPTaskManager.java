package Managers.TaskManager;

import API.Adapters.*;
import API.KVServer.KVTaskClient;
import Tasks.*;
import com.google.gson.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

public class HTTPTaskManager extends InMemoryTaskManager {    // Менеджер для взаимодействия с HTTP-запросами
    private static KVTaskClient client;
    private static Gson gson;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter());
        gson = gsonBuilder.create();
    }

    public HTTPTaskManager() {    // конструктор с регистрацией менеджера
        try {
            client = new KVTaskClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HTTPTaskManager(String API_KEY) {    // конструктор для создания менеджера без регистрации по известному ключу
        client = new KVTaskClient(API_KEY);
    }

    public String getAPI_KEY() {    // возвращает ключ, привязанный к KVTaskClient
        return client.getAPI_KEY();
    }

    public void save() {    // сохраняет состояние по ключу на сервер
        ArrayList<Task> tasks = getAllTasks();
        String jsonTasks = gson.toJson(tasks);
        String jsonHistory = gson.toJson(history());

        client.put(client.getAPI_KEY(), jsonTasks, "tasks");
        client.put(client.getAPI_KEY(), jsonHistory, "history");
    }

    public static HTTPTaskManager loadFromServer(String API_KEY) {    // возвращает HTTPTaskManager и его стостояние по ключу
        String jsonTasks = client.load(API_KEY, "tasks");
        String jsonHistoryArray = client.load(API_KEY, "history");

        JsonArray jsonListOfTasks = gson.fromJson(jsonTasks, JsonArray.class);
        JsonArray jsonListOfHistory = gson.fromJson(jsonHistoryArray, JsonArray.class);

        HTTPTaskManager manager = new HTTPTaskManager(API_KEY);

        for (JsonElement element : jsonListOfTasks) {
            Task task = HTTPTaskManager.fromElementToTask(element);
            if (task instanceof Subtask) {
                Subtask subtask = (Subtask) task;
                subtask.getEpic().updateDurationAndTime();
                subtask.getEpic().updateStatus();
            }
            manager.addTask(task);
        }

        ArrayList<Integer> history = new ArrayList<>();

        for (JsonElement element : jsonListOfHistory) {
            JsonObject jsonObject = element.getAsJsonObject();
            int id = jsonObject.get("id").getAsInt();
            history.add(id);
        }

        Collections.reverse(history);

        for (Integer id : history) {
            manager.getTask(id);
        }

        return manager;
    }
    // вспомогательный метод для конвертации из JsonElement в Task
    private static Task fromElementToTask (JsonElement element) {
        JsonObject jsonObject = element.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        Task task = null;
        switch (type) {
            case "EPIC" -> {
                task = gson.fromJson(element, Epic.class);
            }
            case "SUBTASK" -> task = gson.fromJson(element, Subtask.class);
            default -> task = gson.fromJson(element, Task.class);
        }
        return task;
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void updateTask(Task taskNewVersion) {
        super.updateTask(taskNewVersion);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public void deleteOneTask(int id) {
        super.deleteOneTask(id);
        save();
    }
}
