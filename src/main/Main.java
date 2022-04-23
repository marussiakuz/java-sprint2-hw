import Managers.TaskManager.HTTPTaskManager;
import API.HttpServer.HttpTaskServer;
import API.KVServer.KVServer;
import Tasks.*;
import Enums.StatusOfTask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

public class Main {    // тестирование проекта на этапе финального спринта
    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer kvserver = new KVServer();
        kvserver.start();

        HTTPTaskManager manager = new HTTPTaskManager();

        HttpTaskServer server = new HttpTaskServer(manager);
        server.start();

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

        Epic epic1 = new Epic("Epic1", "has 3 subtasks");
        Subtask subtask1 = new Subtask("Subtask1", "one", epic1.getId(), duration1, date1);
        Subtask subtask2 = new Subtask("Subtask2", "two", epic1.getId(), duration2, date2);
        Subtask subtask3 = new Subtask("Subtask3", "three", epic1.getId(), duration3, date3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        Subtask subtask4 = new Subtask("Subtask4", "four", epic2.getId(), duration5, null);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2.getId(), duration4, date4);

        Task task1 = new Task("Task1", "just task1", duration1, date5);
        Task task2 = new Task("Task2", "just task2");
        Task task3 = new Task("Task3", "just task3");

        manager.addTask(epic1);    // добавляем задачи в Менеджер
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(subtask3);
        manager.addTask(epic2);
        manager.addTask(subtask4);
        manager.addTask(subtask5);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        manager.getTask(1);    // создаем историю просмотра задач по их id
        manager.getTask(4);
        manager.getTask(3);
        manager.getTask(1);
        manager.getTask(9);
        manager.getTask(4);
        manager.getTask(5);
        manager.getTask(6);

        subtask4.setStatus(StatusOfTask.DONE);
        manager.updateTask(subtask4);    // поменяли статус подзадачи c id 6 (относится к epic2)
        manager.deleteOneTask(subtask5.getId());    // удалили подзадачу с id 7 (относится к epic2)*/

        HTTPTaskManager loadedManager = HTTPTaskManager.loadFromServer(manager.getAPI_KEY());

        System.out.println(manager.getAllTasks().equals(loadedManager.getAllTasks()));
        System.out.println(manager.history().equals(loadedManager.history()));

        kvserver.stop();
        server.stop();
    }
}

