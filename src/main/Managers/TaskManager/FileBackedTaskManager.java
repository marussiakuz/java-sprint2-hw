package Managers.TaskManager;

import Enums.*;
import Exceptions.*;
import Managers.HistoryManager.HistoryManager;
import Tasks.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {    // Менеджер с автосохранением
    private String fileTasksInfo;

    public FileBackedTaskManager (String fileTasksInfo) {    // конструктор со строковым обозначением файла
        this.fileTasksInfo = fileTasksInfo;
    }

    public void save() throws ManagerSaveException {    // сохранение задач и списка просмотра в файл
        StringBuilder builder = new StringBuilder("id,type,name,status,description, duration, start, finish,epic\n");
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
            task.getName(), task.getStatus().toString(), task.getDescription(), task.formatDuration(),
            task.formatDate(task.getStartTime()), task.formatDate(task.getEndTime())).collect(Collectors.toList()));
        if (isSubtask(task)) {
            taskInfo = new StringBuilder(taskInfo).append("," + (((Subtask) task).getEpic().getId())).toString();
        }
        return taskInfo;
    }

    public Task taskFromString(String value) {    // создание задачи из строки
        String[] taskInfo = value.split(",");
        Task task = null;
        switch (taskInfo[1]) {
            case "TASK" :
                task = new Task(taskInfo[2], taskInfo[4]);
                restoreDurationAndTime(task, taskInfo[5], taskInfo[6]);
                break;
            case "EPIC" :
                task = new Epic(taskInfo[2], taskInfo[4]);
                break;
            case "SUBTASK" :
                Epic epic = (Epic) getListOfAllTasks().get((Integer.parseInt(taskInfo[8])));
                task = new Subtask(taskInfo[2], taskInfo[4], epic);
                if (restoreDurationAndTime(task, taskInfo[5], taskInfo[6])) epic.updateDurationAndTime();
        }
        task.setStatus(StatusOfTask.valueOf(taskInfo[3]));
        task.setId(Integer.parseInt(taskInfo[0]));
        super.addTask(task);
        return task;
    }
    // восстановить длительность и время старта задачи из строки
    private boolean restoreDurationAndTime (Task task, String duration, String dateTime) {
        if (duration.contains("not set")) return false;
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        LocalDateTime start = dateTime.contains("not set")? null : LocalDateTime.parse(dateTime, formatter);
        try {
            task.setDurationAndStartTime(durationFromString(duration), start);
        } catch (TimeIntersectionException e) {
            task.setDuration(durationFromString(duration));
            task.setStartTime(start);
        }
        return true;
    }

    private Duration durationFromString (String value) {    // преобразование строки в Duration
        String[] data = value.split(" ");
        Duration duration = Duration.ofMinutes(0);
        for (int i = 1; i < data.length; i++) {
            switch (data[i]) {
                case "minutes":
                    duration = duration.plusMinutes(Long.parseLong(data[i - 1]));
                    break;
                case "hours", "hour":
                    duration = duration.plusHours(Long.parseLong(data[i - 1]));
                    break;
                case "days", "day":
                    duration = duration.plusDays(Long.parseLong(data[i - 1]));
            }
        }
        return duration;
    }

    public static String toString(HistoryManager manager) {    // преобразование HistoryManager в строку
        if (manager.getHistory().isEmpty()) return "";
        List<String> history = manager.getHistory().stream().map(Task::getId).map(s -> String.valueOf(s))
            .collect(Collectors.toList());
        return String.join(",", history);
    }

    public static List<Integer> historyFromString(String value) {    // преобразование строки в список просмотра в виде id задач
        if (value.equals("\n")) return null;
        List<Integer> history = Arrays.stream(value.split(",")).map(s -> Integer.parseInt(s)).collect(Collectors
            .toList());
        return history;
    }

    public static FileBackedTaskManager loadFromFile(String file) {    // создание FileBackedTaskManager из файла
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        String content = null;
        try {
            content = Files.readString(Path.of(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<String> tasksAndHistory = Arrays.stream(content.split("\n")).collect(Collectors.toList());
        if(tasksAndHistory.size()==1) return manager;
        for (int i = 1; i < tasksAndHistory.size()-2; i++) {
            manager.taskFromString(tasksAndHistory.get(i));
        }
        List<Integer> history = historyFromString(tasksAndHistory.get(tasksAndHistory.size()-1));
        if (history == null) return manager;
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

        FileBackedTaskManager managerFirst = new FileBackedTaskManager("/Users/Marya/saved.csv");

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
        Subtask subtask1 = new Subtask("Subtask1", "one", epic1, duration1, date1);
        Subtask subtask2 = new Subtask("Subtask2", "two", epic1, duration2, date2);
        Subtask subtask3 = new Subtask("Subtask3", "three", epic1, duration3, date3);

        Epic epic2 = new Epic("Epic2", "has 2 subtasks");
        Subtask subtask4 = new Subtask("Subtask4", "four", epic2, duration5, null);
        Subtask subtask5 = new Subtask("Subtask5", "five", epic2, duration4, date4);

        Task task1 = new Task("Task1", "just task1", duration1, date5);
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
        FileBackedTaskManager managerSecond = FileBackedTaskManager.loadFromFile("/Users/Marya/saved.csv");

        // сравнение сохраненного и восстановленного списка задач
        System.out.println(managerFirst.getAllTasks().equals(managerSecond.getAllTasks()));
        // сравнение сохраненной и восстановленной истории задач
        System.out.println(managerFirst.history().equals(managerSecond.history()));
    }
}
