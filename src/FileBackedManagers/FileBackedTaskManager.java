package FileBackedManagers;

import InMemoryManagers.InMemoryTaskManager;
import Managers.HistoryManager;
import Tasks.Epic;
import Tasks.StatusOfTask;
import Tasks.Subtask;
import Tasks.Task;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {    // Менеджер с автосохранением
    private static String fileTasksInfo;

    public FileBackedTaskManager (String fileTasksInfo) {    // конструктор со строковым обозначением файла
        this.fileTasksInfo = fileTasksInfo;
    }

    public void save() throws ManagerSaveException {    // сохранение задач и списка просмотра в файл
        StringBuilder builder = new StringBuilder("id,type,name,status,description,epic\n");
        for (Task task : this.getAllTasks()) {
            builder.append(toString(task) + "\n");
        }
        builder.append("\n" + toString(this.getInMemoryHistoryManager()));
        try (FileWriter fileWriter = new FileWriter(fileTasksInfo)) {
            fileWriter.write(builder.toString());
        } catch (IOException e) {
            throw new ManagerSaveException();
        }
    }

    public String toString (Task task) {    // преобразование задачи в строковое представление
        String taskInfo = String.join(",", Stream.of(String.valueOf(task.getId()), task.getType().toString(),
            task.getName(), task.getStatus().toString(), task.getDescription()).collect(Collectors.toList()));
        if (isSubtask(task)) {
            taskInfo = new StringBuilder(taskInfo).append("," + (((Subtask) task).getEpic().getId())).toString();
        }
        return taskInfo;
    }

    public Task taskFromString(String value) {    // создание задачи из строки
        String[] taskInfo = value.split(",");
        Task task = null;
        switch (taskInfo[1]) {
            case "TASK" : task = new Task(taskInfo[2], taskInfo[4]);
                break;
            case "EPIC" : task = new Epic(taskInfo[2], taskInfo[4]);
                break;
            case "SUBTASK" : Epic epic = (Epic) getListOfAllTasks().get((Integer.parseInt(taskInfo[5])));
                task = new Subtask(taskInfo[2], taskInfo[4], epic);
                epic.addSubtask((Subtask) task);
        }
        task.setStatus(StatusOfTask.valueOf(taskInfo[3]));
        task.setId(Integer.parseInt(taskInfo[0]));
        super.addTask(task);
        return task;
    }

    static String toString(HistoryManager manager) {    // преобразование HistoryManager в строку
        List<String> history = manager.getHistory().stream().map(Task::getId).map(s -> String.valueOf(s))
            .collect(Collectors.toList());
        return String.join(",", history);
    }

    static List<Integer> historyFromString(String value) {    // преобразование строки в список просмотра в виде id задач
        List<Integer> history = Arrays.stream(value.split(",")).map(s -> Integer.parseInt(s)).collect(Collectors
            .toList());
        return history;
    }

    static FileBackedTaskManager loadFromFile(String file) {    // создание FileBackedTaskManager из файла
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String content = null;
        try {
            content = Files.readString(Path.of(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> tasksAndHistory = Arrays.stream(content.split("\n")).collect(Collectors.toList());
        for (int i = 1; i < tasksAndHistory.size()-2; i++) {
            manager.taskFromString(tasksAndHistory.get(i));
        }
        List<Integer> history = historyFromString(tasksAndHistory.get(tasksAndHistory.size()-1));
        Collections.reverse(history);
        for (Integer id : history) {
            Task task = manager.getListOfAllTasks().get(id);
            manager.getInMemoryHistoryManager().add(task);
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
        FileBackedTaskManager managerFirst = new FileBackedTaskManager("saved.csv");

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
        FileBackedTaskManager managerSecond = FileBackedTaskManager.loadFromFile("saved.csv");

        System.out.println(managerSecond.getAllTasks());    // получаем список всех задач
        System.out.println(managerSecond.history());    // получаем историю просмотренных задач
    }
}
