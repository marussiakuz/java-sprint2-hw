import Managers.TaskManager.InMemoryTaskManager;
import Tasks.*;
import Enums.StatusOfTask;

public class Main {    // тестирование проекта на этапе 4-го спринта (на этапе 5-го - в классе FileBackedTaskManager)
    public static void main(String[] args) {
        Epic epic1 = new Epic("Epic1", "has 3 subtasks");

        Subtask subtask1 = new Subtask("Subtask1", "one", epic1);
        Subtask subtask2 = new Subtask("Subtask2", "two", epic1);
        Subtask subtask3 = new Subtask("Subtask3", "three", epic1);

        Epic epic2 = new Epic("Epic2", "has no subtasks");
        subtask1.setStatus(StatusOfTask.DONE);
        subtask2.setStatus(StatusOfTask.DONE);
        subtask3.setStatus(StatusOfTask.DONE);

        System.out.println(epic1.getListOfSubtasks());

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.addTask(epic1);    // добавляем задачи
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(subtask3);
        manager.addTask(epic2);

        manager.getTask(1);    // создаем историю просмотра задач по их id
        manager.getTask(4);
        manager.getTask(3);
        manager.getTask(1);

        System.out.println(manager.history());    // запрашиваем историю просмотра

        manager.getTask(5);
        manager.getTask(2);
        manager.getTask(1);
        manager.getTask(4);

        System.out.println(manager.history());    // удостоверяемся, что нет повторов

        manager.getTask(5);
        manager.getTask(3);
        manager.getTask(4);
        manager.getTask(3);
        manager.getTask(4);
        manager.getTask(5);
        manager.getTask(4);
        manager.getTask(3);
        manager.getTask(2);
        manager.getTask(1);

        System.out.println(manager.history());

        manager.deleteOneTask(1);    // удаляем эпик, у которого есть три подзадачи

        System.out.println(manager.history());    // снова запрашиваем историю просмотра
    }
}

