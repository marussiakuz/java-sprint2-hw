package FileBackedManagers;

import InMemoryManagers.InMemoryTaskManager;
import Tasks.Epic;
import Tasks.StatusOfTask;
import Tasks.Subtask;
import Tasks.Task;
import java.io.*;

public class FileBackedTaskManager extends InMemoryTaskManager implements Serializable {    // Менеджер с автосохранением
    private static String fileTasksInfo;
    private static final long serialVersionUID = 1L;

    public FileBackedTaskManager (String fileTasksInfo) {    // конструктор со строковым обозначением файла
        this.fileTasksInfo = fileTasksInfo;
    }

    public void save() throws ManagerSaveException {    // сохранение экземпляра класса FileBackedTaskManager в файл
        try (FileOutputStream fileOutput = new FileOutputStream(fileTasksInfo);
            ObjectOutputStream outputStream = new ObjectOutputStream(fileOutput)) {
            outputStream.writeObject(this);
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    static FileBackedTaskManager loadFromFile(String file) {    // восстановление экземпляра класса FileBackedTaskManager из файла
        FileBackedTaskManager manager = null;
        try (FileInputStream fileInput = new FileInputStream(file);
            ObjectInputStream objectStream = new ObjectInputStream(fileInput)) {
            Object object = objectStream.readObject();
            manager = (FileBackedTaskManager)object;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return manager;
    }

    @Override
    public void addTask(Task task) {    // добавить задачу в список
        super.addTask(task);
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateTask(Task taskNewVersion) {    // обновить задачу
        super.updateTask(taskNewVersion);
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAllTasks() {    // удалить все задачи
        super.deleteAllTasks();
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Task getTask(int id) {    // получить задачу по id
        Task task = super.getTask(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
        return task;
    }

    @Override
    public void deleteOneTask(int id) {    // удалить одну задачу
        super.deleteOneTask(id);
        try {
            save();
        } catch (ManagerSaveException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {    // Тестирование функции автосохранения (5-й спринт)
        FileBackedTaskManager managerFirst = new FileBackedTaskManager("savedTasks");

        Epic epic1 = new Epic("Epic1", "has 3 subtasks");
        Subtask subtask1 = new Subtask("Subtask1", "one", epic1);
        Subtask subtask2 = new Subtask("Subtask2", "two", epic1);
        Subtask subtask3 = new Subtask("Subtask3", "three", epic1);
        epic1.addSubtask(subtask1);
        epic1.addSubtask(subtask2);
        epic1.addSubtask(subtask3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        Subtask subtask4 = new Subtask("Subtask4", "four", epic2);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2);
        epic2.addSubtask(subtask4);
        epic2.addSubtask(subtask5);

        Task task1 = new Task("Task1", "just task1");
        Task task2 = new Task("Task2", "just task2");
        Task task3 = new Task("Task3", "just task3");

        managerFirst.addTask(epic1);    // добавляем задачи в Менеджер с функцией автосохранения
        managerFirst.addTask(subtask1);
        managerFirst.addTask(subtask2);
        managerFirst.addTask(subtask3);
        managerFirst.addTask(epic2);
        managerFirst.addTask(subtask4);
        managerFirst.addTask(subtask5);
        managerFirst.addTask(task1);
        managerFirst.addTask(task2);
        managerFirst.addTask(task3);

        managerFirst.getTask(1);    // создаем историю просмотра задач по их id
        managerFirst.getTask(4);
        managerFirst.getTask(3);
        managerFirst.getTask(1);
        managerFirst.getTask(9);
        managerFirst.getTask(4);
        managerFirst.getTask(5);
        managerFirst.getTask(6);
        managerFirst.getTask(7);

        subtask4.setStatus(StatusOfTask.DONE);
        managerFirst.updateTask(subtask4);    // поменяли статус подзадачи c id 6 (относится к epic2)
        managerFirst.deleteOneTask(subtask5.getId());    // удалили подзадачу с id 7 (относится к epic2)

        // воссоздаем ранее сохраненный экземпляр класса FileBackedTaskManager из того же файла
        FileBackedTaskManager managerSecond = FileBackedTaskManager.loadFromFile("savedTasks");

        System.out.println(managerSecond.getAllTasks());    // получаем список всех задач
        System.out.println(managerSecond.history());    // получаем историю просмотренных задач
    }
}
