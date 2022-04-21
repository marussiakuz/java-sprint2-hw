package Tasks;

import Exceptions.TaskNotFoundException;
import static Enums.StatusOfTask.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private volatile static Epic epic;
    private volatile static Subtask subtask1;
    private volatile static Subtask subtask2;
    private volatile static Subtask subtask3;

    @BeforeEach
    public synchronized void beforeEach() {
        epic = new Epic("EpicTest", "for testing");
        subtask1 = new Subtask("sub1", "one", epic.getId());
        subtask2 = new Subtask("sub2", "two", epic.getId());
        subtask3 = new Subtask("sub3", "three", epic.getId());
    }
    // проверка получения списка подзадач
    @Test
    void getListOfSubtasks() {
        epic.getListOfSubtasks().clear();

        assertEquals(NEW, epic.getStatus(), "статус не стал NEW");
        assertTrue(epic.getListOfSubtasks().isEmpty(), "список подзадач не пустой");
    }
    // проверка добавления подзадачи
    @Test
    void addSubtask() {
        Subtask subtask4 = new Subtask("newSubtask", "four", epic.getId());

        assertTrue(epic.getListOfSubtasks().contains(subtask4),"подзадача не добавилась");
        assertTrue(epic.getListOfSubtasks().size() == 4, "размер списка не увеличился");
    }
    // проверка удаления подзадачи
    @Test
    void deleteSubtask() {
        epic.deleteSubtask(subtask1);

        assertFalse(epic.getListOfSubtasks().contains(subtask1), "задача не удалилась из списка");
        assertTrue(epic.getListOfSubtasks().size() == 2, "размер списка не уменьшился");

        subtask3.setStatus(IN_PROGRESS);
        assertTrue(epic.getStatus() == IN_PROGRESS);

        epic.deleteSubtask(subtask3);

        assertTrue(epic.getStatus() == NEW, "после удаления не обновился статус");

        Epic newEpic = new Epic("newEpic", "");
        Subtask newSubtask = new Subtask("newSubtask", "belongs to another epic", newEpic.getId());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> {
            epic.deleteSubtask(newSubtask);
        });
        assertEquals("The subtask belongs to another epic. Call the method from an epic newEpic",
                ex.getMessage());
    }
    // проверка правильности получения конечного времени
    @Test
    void getEndTime() {
        assertNull(epic.getEndTime());

        LocalDateTime dateTime1 = LocalDateTime.of(2022, Month.JUNE, 1, 13, 50);
        LocalDateTime dateTime2 = LocalDateTime.of(2022, Month.JUNE, 1, 16, 00);
        LocalDateTime dateTime3 = LocalDateTime.of(2022, Month.JUNE, 2, 00, 15);
        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofMinutes(30);
        Duration duration3 = Duration.ofDays(1);
        subtask1.setDurationAndStartTime(duration1, dateTime1);
        subtask2.setDurationAndStartTime(duration2, dateTime2);
        subtask3.setDurationAndStartTime(duration3, dateTime3);

        assertEquals(epic.getEndTime(), subtask3.getEndTime(), "конечное время не обновилось");

        epic.deleteSubtask(subtask3);

        assertEquals(epic.getEndTime(), subtask2.getEndTime(), "конечное время не обновилось");
    }
    // проверка обновления статуса задачи
    @Test
    void updateStatus() {
        assertEquals(NEW, epic.getStatus());

        subtask1.setStatus(DONE);
        subtask2.setStatus(DONE);

        assertEquals(NEW, epic.getStatus(), "статус не NEW, когда подзадачи NEW и DONE");

        subtask3.setStatus(DONE);

        assertEquals(DONE, epic.getStatus(), "статус не DONE когда все подзадачи DONE");

        subtask2.setStatus(IN_PROGRESS);

        assertEquals(IN_PROGRESS, epic.getStatus(), "статус не IN_PROGRESS когда есть подзадача с IN_PROGRESS");
    }

    @Test
    void updateDurationAndTime() {
        LocalDateTime dateTime1 = LocalDateTime.of(2022, Month.APRIL, 21, 13, 50);
        LocalDateTime dateTime2 = LocalDateTime.of(2022, Month.APRIL, 21, 16, 00);
        LocalDateTime dateTime3 = LocalDateTime.of(2022, Month.APRIL, 22, 00, 15);
        Duration duration1 = Duration.ofHours(2);
        Duration duration2 = Duration.ofMinutes(30);
        Duration duration3 = Duration.ofDays(1);
        subtask1.setDurationAndStartTime(duration1, dateTime1);
        subtask2.setDurationAndStartTime(duration2, dateTime2);
        subtask3.setDurationAndStartTime(duration3, dateTime3);

        assertEquals(epic.getDuration(), subtask1.getDuration().plus(subtask2.getDuration()).
                plus(subtask3.getDuration()), "длительность эпика не равна сумме длительности его подзадач");

        epic.deleteSubtask(subtask2);

        assertEquals(epic.getDuration(), subtask1.getDuration().plus(subtask3.getDuration()), "после удаления"
                + "подзадачи длительность эпика не пересчиталась");
    }
}