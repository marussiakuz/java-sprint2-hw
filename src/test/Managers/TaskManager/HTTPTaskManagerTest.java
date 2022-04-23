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
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class HTTPTaskManagerTest {

    private static KVServer kvserver;
    private static HTTPTaskManager manager;
    private static HttpTaskServer server;
    private static HttpClient client;
    private static Gson gson;
    private static Epic epic1;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;
    private static Task task1;

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
    public void beforeEach() throws IOException {
       if (hasBeenTasksListAlreadyDeleted()) fillTheManager(manager);
    }

    @AfterEach
    public void afterEach() {
        manager.deleteAllTasks();
    }

    @Test    // проверка получения истории просмотра
    void history() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(gson.toJson(manager.history()), response.body());

        manager.deleteAllTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The history is empty", response.body());
    }

    @Test    // проверка получения задач по дате начала
    void getPrioritizedTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(gson.toJson(manager.getPrioritizedTasks()), response.body(),"возвращенный список " +
                "не соответствует хранимому в памяти");

        manager.deleteAllTasks();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The task list is empty", response.body());
    }

    @Test    // проверка получения всех задач
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

    @Test    // проверка получения всех подзадач определенного эпика
    void getSubtasksOfTheEpic() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/tasks/subtask/epic/?id=%s", epic1.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(gson.toJson(epic1.getListOfSubtasks()), response.body(), "не совпадает с подзадачами " +
                "эпика");

        manager.deleteOneTask(subtask1.getId());
        manager.deleteOneTask(subtask2.getId());
        manager.deleteOneTask(subtask3.getId());
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(String.format("Epic id=%s doesn't have any subtasks", epic1.getId()), response.body());

        URI newUrl = URI.create("http://localhost:8080/tasks/subtask/epic/?id=99");
        request = HttpRequest.newBuilder().uri(newUrl).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The epic with id=99 has not been found", response.body());
    }

    @Test    // проверка добавления новой задачи
    void addTask() throws IOException, InterruptedException {
        Duration oldDurationOfTheEpic = epic1.getDuration();
        LocalDateTime dateOfNewSubtask = LocalDateTime.of(2022, Month.MAY, 1, 8, 0);
        Duration durationOfNewSubtask = Duration.ofHours(5);
        Subtask newSubtask = new Subtask("Added Subtask", "fourth", epic1.getId(), durationOfNewSubtask,
                dateOfNewSubtask);

        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        String json = gson.toJson(newSubtask);
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the subtask has been successfully added", response.body());
        assertTrue(manager.getAllTasks().contains(newSubtask), "задача не добавилась");
        assertTrue(epic1.getListOfSubtasks().contains(newSubtask), "подзадача не добавилась в список "
                + "подзадач эпика");
        assertEquals(epic1.getStartTime(), dateOfNewSubtask, "дата старта эпика не обновилась");
        assertEquals(oldDurationOfTheEpic.plus(durationOfNewSubtask), epic1.getDuration(), "длительность эпика "
                + "не обновилась");

        json = "{\"name\":\"subtaskWithNotExistingEpic\",\"description\":\"none\",\"epicId\":99}";
        final HttpRequest.BodyPublisher secondBody = HttpRequest.BodyPublishers.ofString(json);
        request = HttpRequest.newBuilder().uri(url).POST(secondBody).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String allExistingEpics = manager.getAllTasks().stream().filter(task -> InMemoryTaskManager.isEpic(task))
                .map(task -> task.getId()).map(String::valueOf).collect(Collectors.joining(","));

        assertEquals(String.format("Epic with id=99 does not exist. Choose existing epics with the following id: %s "
                + "or create a new Epic with id=99 first", allExistingEpics), response.body());
    }

    @Test    // проверка обновления уже существующей задачи
    void updateTask() throws IOException, InterruptedException {
        subtask1.setStatus(StatusOfTask.IN_PROGRESS);
        String json = gson.toJson(subtask1);
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        final HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(body).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the subtask has been successfully updated", response.body());
        assertEquals(StatusOfTask.IN_PROGRESS, epic1.getStatus(), "данные эпика не обновились");
    }

    @Test    // проверка удаления всех задач
    void deleteAllTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("All tasks have been successfully deleted", response.body());
        assertTrue(manager.getAllTasks().isEmpty(), "задачи не удалились");
    }

    @Test    // проверка получения задачи по id
    void getTask() throws IOException, InterruptedException {
        URI url = URI.create(String.format("http://localhost:8080/tasks/task/?id=%s", epic1.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(gson.toJson(epic1), response.body(), "не получили запрашиваемую задачу");

        url = URI.create("http://localhost:8080/tasks/task/?id=99");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("The task with id=99 has not been found", response.body());
    }

    @Test    // проверка удаления одной задачи
    void deleteOneTask() throws IOException, InterruptedException {
        subtask1.setStatus(StatusOfTask.IN_PROGRESS);
        manager.updateTask(subtask1);
        assertEquals(StatusOfTask.IN_PROGRESS ,epic1.getStatus());

        URI url = URI.create(String.format("http://localhost:8080/tasks/task/?id=%s", subtask1.getId()));
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(String.format("the task with id=%s has been successfully deleted", subtask1.getId()),
                response.body());
        assertFalse(manager.getAllTasks().contains(subtask1), "задача не была удалена из списка менеджера");
        assertEquals(StatusOfTask.NEW, epic1.getStatus(), "после удаления подзадачи статус эпика не изменился");
        assertFalse(epic1.getListOfSubtasks().contains(subtask1), "задача не была удалена из списка подзадач "
                + "эпика");

        url = URI.create(String.format("http://localhost:8080/tasks/task/?id=%s", epic1.getId()));
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals("the epic with id=1 has been successfully deleted, "
                + "all its subtasks with id 3,4 have also been deleted", response.body());
        assertFalse(manager.getListOfAllTasks().containsKey(3),"подзадача 3 не была удалена вместе с эпиком");
        assertFalse(manager.getListOfAllTasks().containsKey(4),"подзадача 4 не была удалена вместе с эпиком");

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
        subtask2 = new Subtask("Subtask2", "two", epic1.getId(), duration2, date2);
        subtask3 = new Subtask("Subtask3", "three", epic1.getId(), duration3, date3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        Subtask subtask4 = new Subtask("Subtask4", "four", epic2.getId(), duration5, null);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2.getId(), duration4, date4);

        task1 = new Task("Task1", "just task1", duration1, date5);
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

        manager.getTask(epic1.getId());    // создаем историю просмотра задач по их id
        manager.getTask(subtask2.getId());
        manager.getTask(task2.getId());
        manager.getTask(subtask3.getId());
        manager.getTask(epic2.getId());
        manager.getTask(subtask4.getId());
        manager.getTask(subtask3.getId());
        manager.getTask(subtask5.getId());
        manager.getTask(epic1.getId());
    }

    // вспомогательный метод для проверки были ли удалены уже все задачи в процессе тестирования
    private boolean hasBeenTasksListAlreadyDeleted (){
        return manager.getAllTasks().isEmpty();
    }
}