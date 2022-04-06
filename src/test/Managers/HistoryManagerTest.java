package Managers;

import Managers.InMemoryHistoryManager;
import Managers.InMemoryTaskManager;
import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;
import Tasks.TaskNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private InMemoryTaskManager taskManager;
    private Epic epic;
    private Subtask subtask1;
    private Subtask subtask2;
    private Subtask subtask3;
    private Task task;

    @BeforeEach
    void setUp() {
        taskManager = new InMemoryTaskManager();
        historyManager = taskManager.getInMemoryHistoryManager();
        epic = new Epic("EpicTest", "for testing");
        subtask1 = new Subtask("sub1", "one", epic);
        subtask2 = new Subtask("sub2", "two", epic);
        subtask3 = new Subtask("sub3", "three", epic);
        task = new Task("Task", "oneTask");
        taskManager.addTask(epic);
        taskManager.addTask(subtask1);
        taskManager.addTask(subtask2);
        taskManager.addTask(subtask3);
        taskManager.addTask(task);
    }
    // проверка добавления задачи в историю
    @Test
    void add() {
        assertTrue(historyManager.getHistory().isEmpty());

        taskManager.getTask(epic.getId());
        taskManager.getTask(task.getId());
        taskManager.getTask(subtask1.getId());

        assertTrue(historyManager.getHistory().size() == 3, "размер списка не изменился");
        assertTrue(historyManager.getHistory().contains(epic), "задачи нет в списке");

        taskManager.getTask(epic.getId());

        assertTrue(historyManager.getHistory().size() == 3, "задача добавилась дважды");
        assertEquals(epic, historyManager.getHistory().get(0));
        assertEquals(task, historyManager.getHistory().get(2));
    }
    // проверка удаления задачи
    @Test
    void remove() {
        taskManager.getTask(epic.getId());

        assertTrue(historyManager.getHistory().size() == 1);

        historyManager.remove(epic.getId());

        assertTrue(historyManager.getHistory().isEmpty(), "при удалении последней задачи список не пуст");

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            historyManager.remove(5);
        });
        assertEquals("The task with id=5 does not exist", ex.getMessage());
    }
    // проверка получения истории задач
    @Test
    void getHistory() {
        taskManager.getTask(epic.getId());
        taskManager.getTask(task.getId());
        taskManager.getTask(subtask1.getId());
        taskManager.getTask(subtask2.getId());
        taskManager.getTask(subtask3.getId());
        taskManager.getTask(subtask1.getId());
        taskManager.getTask(epic.getId());

        ArrayList<Task> history = new ArrayList(Arrays.asList(epic, subtask1, subtask3, subtask2, task));
        assertEquals(history, historyManager.getHistory(), "несоответствие просмотра и истории задач");
    }
}