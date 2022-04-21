package Managers.TaskManager;

import API.Adapters.*;
import API.HttpServer.HttpTaskServer;
import API.KVServer.KVServer;
import Enums.StatusOfTask;
import Tasks.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskManagerTest {

    private static KVServer kvserver;
    private static HTTPTaskManager manager;
    private static HttpTaskServer server;
    private static HttpClient client;
    private static Gson gson;
    private static Epic epic1;
    private static Subtask subtask1;

    @BeforeAll
    public static void prepareForTesting() throws IOException {
        kvserver = new KVServer();
        kvserver.start();
        client = HttpClient.newHttpClient();
        manager = new HTTPTaskManager();
        server = new HttpTaskServer(manager);
        fillTheManager(manager);
        server.start();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter());
        gson = gsonBuilder.create();
    }

    @BeforeEach
    public void beforeEach() {
        if (isTasksListAlreadyDeleted()) fillTheManager(manager);
    }

    // проверка получения истории просмотра
    @Test
    void history() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.body(), gson.toJson(manager.history()));
    }

    // проверка получения задач по дате начала
    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.body(), gson.toJson(manager.getPrioritizedTasks()),"возвращенный список " +
                "не соответствует хранимому в памяти");

        manager.deleteAllTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The task list is empty", response.body());
    }

    // проверка получения всех задач
    @Test
    void getAllTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.body(), gson.toJson(manager.getAllTasks()),"возвращенный список " +
                "не соответствует хранимому в памяти");

        manager.deleteAllTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The task list is empty", response.body());
    }

    // проверка получения всех подзадач определенного эпика
    @Test
    void getSubtasksOfTheEpic() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/tasks/subtask/epic/?id=%s", epic1.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.body(), gson.toJson(epic1.getListOfSubtasks()), "не совпадает с подзадачами эпика");

        URI newUrl = URI.create("http://localhost:8080/tasks/subtask/epic/?id=99");
        request = HttpRequest.newBuilder().uri(newUrl).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The epic with id=99 has not been found", response.body());
    }

    // проверка добавления новой задачи
    @Test
    void addTask() throws IOException, InterruptedException {
        LocalDateTime date = LocalDateTime.of(2022, Month.MAY, 1, 8, 0);
        Duration duration = Duration.ofHours(5);
        Subtask newSubtask = new Subtask("Added Subtask", "fourth", epic1.getId(), duration, date);

        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        String json = gson.toJson(newSubtask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the subtask has been successfully added", response.body());
        assertTrue(manager.getAllTasks().contains(newSubtask), "задача не добавилась");
        assertEquals(epic1.getStartTime(), date, "данные эпика не обновились");
    }

    // проверка обновления уже существующей задачи
    @Test
    void updateTask() throws IOException, InterruptedException {
        subtask1.setStatus(StatusOfTask.IN_PROGRESS);
        String json = gson.toJson(subtask1);
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the subtask has been successfully updated", response.body());
        assertEquals(epic1.getStatus(), StatusOfTask.IN_PROGRESS, "данные эпика не обновились");
    }

    // проверка удаления всех задач
    @Test
    void deleteAllTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("All tasks have been successfully deleted", response.body());
        assertTrue(manager.getAllTasks().isEmpty(), "задачи не удалились");
    }

    // проверка получения задачи по id
    @Test
    void getTask() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/tasks/task/?id=%s", epic1.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(response.body(), gson.toJson(epic1), "не получили запрашиваемую задачу");

        url = URI.create("http://localhost:8080/tasks/task/?id=99");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The task with id=99 has not been found", response.body());
    }

    // проверка удаления одной задачи
    @Test
    void deleteOneTask() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the epic with id=5 has been successfully deleted, "
                + "all its subtasks with id 6,7 have also been deleted", response.body());
        assertFalse(manager.getListOfAllTasks().containsKey(6),"подзадача не была удалена вместе с эпиком");
        assertFalse(manager.getListOfAllTasks().containsKey(7),"подзадача не была удалена вместе с эпиком");

        url = URI.create("http://localhost:8080/tasks/task/?id=101");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the task with id=101 has not been found", response.body());
    }

    // вспомогательный метод, заполняющий Менеджер задачами перед тестированием
    private static void fillTheManager(HTTPTaskManager manager) {
        LocalDateTime date1 = LocalDateTime.of(2022, Month.MAY, 2, 13, 30);
        LocalDateTime date2 = LocalDateTime.of(2022,Month.MAY, 2, 15, 30);
        LocalDateTime date3 = LocalDateTime.of(2022,Month.MAY, 3, 15, 30);
        LocalDateTime date4 = LocalDateTime.of(2022,Month.MAY, 1, 17, 30);
        LocalDateTime date5 = LocalDateTime.of(2022,Month.MAY, 3, 17, 30);

        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofDays(1);
        Duration duration3 = Duration.ofMinutes(90);
        Duration duration4 = Duration.ofHours(3);
        Duration duration5 = Duration.ofMinutes(180);

        epic1 = new Epic("Epic1", "has 3 subtasks");
        subtask1 = new Subtask("Subtask1", "one", epic1.getId(), duration1, date1);
        Subtask subtask2 = new Subtask("Subtask2", "two", epic1.getId(), duration2, date2);
        Subtask subtask3 = new Subtask("Subtask3", "three", epic1.getId(), duration3, date3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        Subtask subtask4 = new Subtask("Subtask4", "four", epic2.getId(), duration5, null);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2.getId(), duration4, date4);

        Task task1 = new Task("Task1", "just task1", duration1, date5);
        Task task2 = new Task("Task2", "just task2");
        Task task3 = new Task("Task3", "just task3");

        manager.addTask(epic1);    // добавляем задачи в Менеджер с функцией автосохранения
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(subtask3);
        manager.addTask(epic2);
        manager.addTask(subtask4);
        manager.addTask(subtask5);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
    }

    // вспомогательный метод для проверки были ли удалены уже все задачи в процессе тестирования
    private boolean isTasksListAlreadyDeleted (){
        return manager.getAllTasks().isEmpty();
    }
}