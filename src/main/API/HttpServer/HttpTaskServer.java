package API.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;

import API.Adapters.*;
import Enums.StatusOfTask;
import Managers.TaskManager.HTTPTaskManager;
import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;
import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

public class HttpTaskServer {    // сервер для маппинга запросов на методы менеджера HTTPTaskManager
    private static final int PORT = 8080;
    private HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    private HTTPTaskManager manager;
    private static Gson gson;
    private String response;
    private int rCode;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(StatusOfTask.class, new StatusOfTaskAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .registerTypeAdapter(Task.class, new TaskAdapter());
        gson = gsonBuilder.create();
    }

    public HttpTaskServer(HTTPTaskManager manager) throws IOException {    // конструктор с реализацией в нем маппинга запросов
        this.manager = manager;
        httpServer.createContext("/tasks", (h) -> {
            try {
                switch (h.getRequestMethod()) {
                    case "GET" -> getTaskInfo(Path.of(h.getRequestURI().getPath()), h.getRequestURI().getQuery());
                    case "POST" -> {
                        InputStream inputStream = h.getRequestBody();
                        String taskInfo = new String(inputStream.readAllBytes());
                        addTask(taskInfo, Path.of(h.getRequestURI().getPath()));
                    }
                    case "DELETE" -> deleteTaskInfo(h.getRequestURI().getQuery());
                    default -> h.sendResponseHeaders(501, 0);
                }
                h.sendResponseHeaders(rCode, 0);
                try (OutputStream os = h.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } finally {
                h.close();
            }
        });
    }

    private void getTaskInfo(Path path, String query) {    // метод для обработки GET-запросов
        int id = getId(query);
        if (id >= 0 && !manager.getListOfAllTasks().containsKey(id)) {
            response = String.format("The %s with id=%d has not been found", path.getFileName(), id);
            rCode = 404;
            return;
        }
        switch (path.getFileName().toString()) {
            case "task" -> {
                if (id >= 0) response = gson.toJson(manager.getTask(id));
                else {
                    if (manager.getAllTasks().isEmpty()) response = "The task list is empty";
                    else response = gson.toJson(manager.getAllTasks());
                }
            }
            case "history" -> {
                if (manager.history().isEmpty()) response = "The history is empty";
                else response = gson.toJson(manager.history());
            }
            case "epic" -> {
                if (!manager.getListOfAllTasks().containsKey(id)) {
                    sendEpicNotFoundMessage(id);
                    return;
                }
                Epic epic = (Epic) manager.getTask(id);
                if (epic.getListOfSubtasks().isEmpty()) response = String.format("Epic id=%s doesn't have any subtasks",
                        epic.getId());
                else response = gson.toJson(epic.getListOfSubtasks());
            }
            case "tasks" -> {
                if (manager.getPrioritizedTasks().isEmpty()) response = "The task list is empty";
                else response = gson.toJson(manager.getPrioritizedTasks());
            }
        }
        rCode = 200;
    }

    private void addTask(String taskInfo, Path path) {    // метод для обработки POST-запросов
        JsonElement jsonElement = JsonParser.parseString(taskInfo);
        if (!jsonElement.isJsonObject()) {
            response = "the request body has not been recognised";
            rCode = 400;
        }
        Task task = null;
        switch (path.getFileName().toString()) {
            case "epic" -> {
                task = gson.fromJson(taskInfo, Epic.class);
                checkIdAndStatus(task);
            }
            case "subtask" -> {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                int epicId = jsonObject.get("epicId").getAsInt();
                if (!isEpicExists(epicId)) {
                    sendEpicNotFoundMessage(epicId);
                    return;
                }
                task = gson.fromJson(taskInfo, Subtask.class);
                checkIdAndStatus(task);
                if (!manager.getListOfAllTasks().containsKey(task.getId())) {
                    Epic epic = (Epic) manager.getListOfAllTasks().get(epicId);
                    epic.addSubtask((Subtask) task);
                }
            }
            default -> {
                task = gson.fromJson(taskInfo, Task.class);
                checkIdAndStatus(task);
            }
        }
        if (manager.getListOfAllTasks().containsKey(task.getId())) {
            manager.updateTask(task);
            response = String.format("the %s has been successfully updated", task.getType().toString().toLowerCase());
        } else {
            manager.addTask(task);
            response = String.format("the %s has been successfully added", task.getType().toString().toLowerCase());
        }
        rCode = 201;
    }

    private void checkIdAndStatus(Task task) {    // если не указан id задачи, присваивает id, следующий в порядке возрастания
        if (task.getId() == 0) task.setId(nextId());
        if (task.getStatus() == null) task.setStatus(StatusOfTask.NEW);
    }

    private int nextId() {    // определяет последний номер присвоенного id и возвращает его инкремент
        if (manager.getListOfAllTasks().size() == 0) return 1;
        TreeSet<Integer> idNumbers = new TreeSet<>(manager.getListOfAllTasks().keySet());
        return idNumbers.last() + 1;
    }

    private boolean isEpicExists (int epicId) {    // проверяет существует ли эпик с заданным id
        return manager.getListOfAllTasks().containsKey(epicId) && manager.isEpic(manager.getTask(epicId));
    }

    private void sendEpicNotFoundMessage(int epicId) {    // формирует сообщение, что эпика с таким id нет, предлагает другие эпики
        StringJoiner joiner = new StringJoiner(",");
        for (Task task : manager.getAllTasks()) {
            if (task instanceof Epic) joiner.add(String.valueOf(task.getId()));
        }
        String epicsId = joiner.toString();
        response = String.format("Epic with id=%s does not exist. Choose existing epics with the following id: "
                + "%s or create a new Epic with id=%s first", epicId, epicsId, epicId);
        rCode = 400;
    }

    private void deleteTaskInfo(String query) {    // обрабатывает DELETE-запросы
        if (query == null) {
            rCode = 200;
            response = "All tasks have been successfully deleted";
            manager.deleteAllTasks();
        } else {
            int id = getId(query);
            if (!manager.getListOfAllTasks().containsKey(id)) {
                rCode = 404;
                response = String.format("the task with id=%d has not been found", id);
            } else {
                if (manager.getListOfAllTasks().get(id).getClass() == Epic.class) {
                    String subtasksOfThisEpic = ((Epic) manager.getListOfAllTasks().get(id)).getListOfSubtasks()
                            .stream().map(subtask -> subtask.getId()).map(String::valueOf)
                            .collect(Collectors.joining(","));
                    response = String.format("the epic with id=%s has been successfully deleted, "
                            + "all its subtasks with id %s have also been deleted", id, subtasksOfThisEpic);
                } else response = String.format("the task with id=%s has been successfully deleted", id);
                manager.deleteOneTask(id);
                rCode = 200;
            }
        }
    }

    private int getId(String query) {    // возвращает id из запроса
        return query == null ? -1 : Integer.parseInt(query.substring(query.indexOf("id=") + 3));
    }

    public void start() throws IOException {    // запуск сервера
        httpServer.start();
    }

    public void stop() {    // остановка сервера
        httpServer.stop(0);
    }
}
