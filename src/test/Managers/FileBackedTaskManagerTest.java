package Managers;

import FileBackedManagers.FileBackedTaskManager;
import Tasks.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends TaskManagerTest<TaskManager> {

    private static FileBackedTaskManager manager2;
    private static Task task2;
    private static Epic epic1;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;
    private static Subtask subtask4;

    @Override
    public TaskManager setManager() {
        return new FileBackedTaskManager("/Users/Marya/savedOne.csv");
    }

    @BeforeAll
    public static void setUp() {
        manager2 = new FileBackedTaskManager("/Users/Marya/savedTwo.csv");

        LocalDateTime date1 = LocalDateTime.of(2022, Month.NOVEMBER, 2, 13, 30);
        LocalDateTime date2 = LocalDateTime.of(2022,Month.NOVEMBER, 2, 15, 30);
        LocalDateTime date3 = LocalDateTime.of(2022,Month.NOVEMBER, 3, 15, 30);
        LocalDateTime date4 = LocalDateTime.of(2022,Month.NOVEMBER, 1, 17, 30);

        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofDays(1);
        Duration duration3 = Duration.ofMinutes(90);
        Duration duration4 = Duration.ofHours(3);
        Duration duration5 = Duration.ofMinutes(180);

        epic1 = new Epic("Epic1", "has 3 subtasks");
        subtask1 = new Subtask("Subtask1", "one", epic1, duration1, date1);
        subtask2 = new Subtask("Subtask2", "two", epic1, duration2, date2);
        subtask3 = new Subtask("Subtask3", "three", epic1, duration3, date3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        subtask4 = new Subtask("Subtask4", "four", epic2, duration5, null);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2, duration4, date4);

        Task task1 = new Task("Task2", "just task2");
        task2 = new Task("Task3", "just task3");

        manager2.addTask(epic1);
        manager2.addTask(subtask1);
        manager2.addTask(subtask2);
        manager2.addTask(subtask3);
        manager2.addTask(epic2);
        manager2.addTask(subtask4);
        manager2.addTask(subtask5);
        manager2.addTask(task1);
        manager2.addTask(task2);
        manager2.getTask(subtask4.getId());
        manager2.getTask(subtask2.getId());
        manager2.getTask(subtask1.getId());
        manager2.getTask(task2.getId());

    }
    // проверка сохранения задач и истороии
    @Test
    public void save() {
        String taskString = manager2.toString(task2);
        Task taskFromString = manager2.taskFromString(taskString);

        assertEquals(task2, taskFromString,"восстановленная задача не соответствует сохраненной");

        epic1.deleteSubtask(subtask1);
        epic1.deleteSubtask(subtask2);
        epic1.deleteSubtask(subtask3);

        String epicString = manager2.toString(epic1);
        Task epicFromString = manager2.taskFromString(epicString);

        assertEquals(epicFromString, epic1,"неправильное сохранение эпика без подзадач");

        String historyString = FileBackedTaskManager.toString(manager2.getInMemoryHistoryManager());
        List<Integer> history = FileBackedTaskManager.historyFromString(historyString);
        List<Integer> expected = new ArrayList<>(List.of(task2.getId(), subtask1.getId(), subtask2.getId(),
                subtask4.getId()));

        assertEquals(history, expected,"восстановленная история задач не соответствует списку просмотренных id");
    }

    @Test
    public void loadFromFile() {
        FileBackedTaskManager manager3 = FileBackedTaskManager.loadFromFile("/Users/Marya/savedTwo.csv");

        assertEquals(manager3.history(), manager2.history(),"списки с историей не идентичны");
        assertEquals(manager3.getAllTasks(), manager2.getAllTasks(),"списки задач не идентичны");

        manager2.deleteAllTasks();
        FileBackedTaskManager manager4 = FileBackedTaskManager.loadFromFile("/Users/Marya/savedTwo.csv");

        assertTrue(manager4.getAllTasks().isEmpty(),"список задач не очищается после удаления");
        assertTrue(manager4.history().isEmpty(),"список с историей не очищается после удаления всех задач");
    }
}
