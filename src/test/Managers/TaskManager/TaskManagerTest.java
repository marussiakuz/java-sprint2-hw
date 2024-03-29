package Managers.TaskManager;

import Enums.StatusOfTask;
import Exceptions.TaskNotFoundException;
import Exceptions.TimeIntersectionException;
import Tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest <T extends TaskManager> {

    public T manager;
    public Epic epic;
    public Subtask subtask1;
    public Subtask subtask2;
    public Subtask subtask3;
    public Task task;

    public abstract T setManager();    // абстрактный метод для инициализации дженерика

    @BeforeEach
    public void beforeEach() {
        manager = setManager();
        epic = new Epic("EpicTest", "for testing");
        subtask1 = new Subtask("sub1", "one", epic.getId());
        subtask2 = new Subtask("sub2", "two", epic.getId());
        subtask3 = new Subtask("sub3", "three", epic.getId());
        task = new Task("Task", "oneTask");
        manager.addTask(epic);
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(subtask3);
        manager.addTask(task);
    }
    // проверка получения списка всех задач
    @Test
    void getAllTasks() {
        assertEquals(5, manager.getAllTasks().size(), "размер списка не соответствует количеству" +
                "добавленных задач");
    }
    // проверка удаления всех задач
    @Test
    void deleteAllTasks() {
        manager.deleteAllTasks();
        assertTrue(manager.getAllTasks().isEmpty(), "после удаления список не пустой");
    }
    // проверка получения задачи
    @Test
    void getTask() {
        assertEquals(epic, manager.getTask(epic.getId()), "полученная задача не соответствует запрошенной");

        manager.deleteOneTask(epic.getId());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            manager.getTask(epic.getId());
        });
        assertEquals(String.format("The task with id=%d does not exist", epic.getId()), ex.getMessage());
    }
    // проверка добавления задачи
    @Test
    void addTask() {
        Subtask newSubtask = new Subtask("newSubtask", "four", epic.getId());
        final int idNewTask = newSubtask.getId();
        newSubtask.setStatus(StatusOfTask.IN_PROGRESS);
        manager.addTask(newSubtask);

        assertEquals(epic.getStatus(), StatusOfTask.IN_PROGRESS, "после добавления новой подзадачи " +
                "статус эпика не изменился");
        assertNotNull(manager.getTask(idNewTask), "задача равна null");
        assertEquals(newSubtask, manager.getTask(idNewTask), "возвращает не ту задачу");
        assertTrue(manager.getAllTasks().contains(newSubtask), "в списке нет добавленной задачи");

        Duration duration = Duration.ofHours(3);
        LocalDateTime dateOne = LocalDateTime.of(2022, Month.APRIL, 8, 15, 15);
        LocalDateTime dateTwo = LocalDateTime.of(2022, Month.APRIL, 8, 17, 15);

        TimeIntersectionException ex = assertThrows(TimeIntersectionException.class, () -> {
            manager.addTask(new Subtask("SubOne", "", epic.getId(), duration, dateOne));
            manager.addTask(new Subtask("SubTwo", "", epic.getId(), duration, dateTwo));
        });
        assertEquals("The selected time is not available, the nearest available time is 08.04.2022 18:15",
                ex.getMessage());
    }
    // проверка обновления задачи
    @Test
    void updateTask() {
        task.setStatus(StatusOfTask.IN_PROGRESS);
        task.setDuration(Duration.ofMinutes(45));
        manager.updateTask(task);

        assertEquals(manager.getTask(task.getId()).getStatus(), StatusOfTask.IN_PROGRESS, "статус остался " +
                "прежний");
        assertEquals(manager.getTask(task.getId()).getDuration(), Duration.ofMinutes(45), "длительность задачи" +
                "не изменилась");

        manager.deleteAllTasks();

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            manager.updateTask(task);
        });
        assertEquals("The task has not been found in the manager's task list", ex.getMessage());
    }
    // проверка удаления одной задачи
    @Test
    void deleteOneTask() {
        manager.deleteOneTask(task.getId());

        assertFalse(manager.getAllTasks().contains(task), "задача не была удалена");

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            manager.deleteOneTask(15);
        });
        assertEquals("The task with id=15 does not exist", ex.getMessage());

        Subtask newSubtask = new Subtask("newSubtask", "four", epic.getId());
        newSubtask.setStatus(StatusOfTask.IN_PROGRESS);
        manager.addTask(newSubtask);

        assertEquals(epic.getStatus(), StatusOfTask.IN_PROGRESS);

        manager.deleteOneTask(newSubtask.getId());

        assertEquals(epic.getStatus(), StatusOfTask.NEW, "после удаления подзадачи статус эпика неи изменился");
    }
}