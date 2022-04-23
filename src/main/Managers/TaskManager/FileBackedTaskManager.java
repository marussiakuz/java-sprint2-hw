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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {    // Менеджер с автосохранением
    private String fileTasksInfo;

    public FileBackedTaskManager() {
    }

    public FileBackedTaskManager(String fileTasksInfo) {    // конструктор со строковым обозначением файла
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
            taskInfo = new StringBuilder(taskInfo).append("," + (((Subtask) task).getEpicId())).toString();
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
                int epicId = Integer.parseInt(taskInfo[8]);
                task = new Subtask(taskInfo[2], taskInfo[4], epicId);
                if (restoreDurationAndTime(task, taskInfo[5], taskInfo[6])) getEpic(epicId).updateDurationAndTime();
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

    public static Duration durationFromString (String value) {    // преобразование строки в Duration
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
        boolean hasHistory = false;
        List<Integer> history = null;
        for (int i = 1; i < tasksAndHistory.size(); i++) {
            if (i == tasksAndHistory.size() - 2 && tasksAndHistory.get(i).isEmpty()) {
                hasHistory = true;
                continue;
            }
            if (i == tasksAndHistory.size() - 1 && hasHistory == true) {
                history = historyFromString(tasksAndHistory.get(tasksAndHistory.size() - 1));
                Collections.reverse(history);
                break;
            }
            manager.taskFromString(tasksAndHistory.get(i));
        }
        if (history == null) return manager;
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
}
