import InMemoryManagers.InMemoryTaskManager;
import Tasks.Epic;
import Tasks.Subtask;
import Tasks.Task;

public class Main {    // тестирование проекта на этапе 3-его спринта
    public static void main(String[] args) {
        Epic epic1 = new Epic("Проект второго спринта", "Менеджер задач");
        Subtask subtask1 = new Subtask("Реализация", "создать проект Менеджер задач согласно ТЗ",
            epic1);
        Subtask subtask2 = new Subtask("Тестирование", "пройти тестирование", epic1);
        epic1.addSubtask(subtask1);
        epic1.addSubtask(subtask2);

        Epic epic2 = new Epic("Сдача проекта второго спринта", "Менеджер задач");
        Subtask subtask = new Subtask("Внесение исправлений", "Внести все исправления согласно ревью",
            epic2);
        epic2.addSubtask(subtask);

        Task task = new Task("файл README", "указать описание в файле README");

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.addTask(epic1);    // добавляем задачи
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(epic2);
        manager.addTask(subtask);
        manager.addTask(task);

        manager.getTask(1);    // создаем историю просмотра получением задач по их id номеру
        manager.getTask(5);
        manager.getTask(4);
        manager.getTask(6);
        manager.getTask(2);
        manager.getTask(3);
        manager.getTask(4);
        manager.getTask(4);
        manager.getTask(6);
        manager.getTask(5);
        manager.getTask(4);
        manager.getTask(3);
        manager.getTask(2);
        manager.getTask(1);

        System.out.println("Распечатаем список просмотренных задач:");
        System.out.println(manager.history());    // распечатаем полученный список
        System.out.println("____________________________________________________________________");

        manager.deleteOneTask(4);    // удаляем эпик c id 4, автоматически удаляется и его подзадача с id 5
        System.out.println("Распечатаем обновленный список просмотра с учетом удаленного эпика и его подзадачи:");
        System.out.println(manager.history());
        System.out.println("____________________________________________________________________");
    }
}

