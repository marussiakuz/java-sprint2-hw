package Managers.TaskManager;

import Exceptions.TimeIntersectionException;
import Managers.HistoryManager.InMemoryHistoryManager;
import Tasks.*;
import Exceptions.TaskNotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {    // Менеджер задач в оперативной памяти
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();
    private TreeSet<Task> prioritizedTasks = new TreeSet<>();
    private transient InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();
    private transient TimeIntersectionChecker timeChecker = new TimeIntersectionChecker();

    public List<Task> history() {    // получить список просмотренных задач
        return inMemoryHistoryManager.getHistory();
    }

    public InMemoryHistoryManager getInMemoryHistoryManager() {    // возвращает объект inMemoryHistoryManager
        return inMemoryHistoryManager;
    }

    public HashMap<Integer, Task> getListOfAllTasks() {    // возвращает мапу с задачами и id - ключами
        return listOfAllTasks;
    }

    public TreeSet<Task> getPrioritizedTasks() {    // получить список задач с сортировкой по времени
        prioritizedTasks.addAll(listOfAllTasks.values());
        return prioritizedTasks;
    }

    public void clearIntersectionChecker() {    // очистить временную сетку (сделать все периоды == true)
        timeChecker = timeChecker.updateTimeIntersectionChecker();
    }

    public Epic getEpic (int id) {    // получить эпик по его id
        return (Epic) listOfAllTasks.get(id);
    }

    @Override
    public ArrayList<Task> getAllTasks() {    // получить список всех задач
        return (ArrayList<Task>) listOfAllTasks.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {    // удалить все задачи
        listOfAllTasks.clear();
        prioritizedTasks.clear();
        inMemoryHistoryManager.clear();
        clearIntersectionChecker();
    }

    @Override
    public Task getTask(int id) {    // получить информацию о задаче по её id
        if (!listOfAllTasks.containsKey(id))
            throw new TaskNotFoundException(String.format("The task with id=%d does not exist", id));
        Task task = listOfAllTasks.get(id);
        inMemoryHistoryManager.add(task);
        return task;
    }

    @Override
    public void addTask(Task task) {    // добавить в список задачу
        if (hasTaskDurationAndTime(task)) checkTimeAvailability(task.getDuration(), task.getStartTime());
        listOfAllTasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        if (isSubtask(task)) {
            Subtask subtask = (Subtask) task;
            Epic epic = (Epic) listOfAllTasks.get(subtask.getEpicId());
            epic.addSubtask(subtask);    // обновление статуса и времени эпика происходит при добавлении подзадачи
        }
    }

    @Override
    public void updateTask(Task taskNewVersion) {    // обновить задачу
        if (!getListOfAllTasks().containsKey(taskNewVersion.getId()))
            throw new TaskNotFoundException("The task has not been found in the manager's task list");
        if (hasTaskDurationAndTime(taskNewVersion)) updateTime(taskNewVersion.getId(), taskNewVersion.getDuration(),
                taskNewVersion.getStartTime());
        listOfAllTasks.put(taskNewVersion.getId(), taskNewVersion);
        if (isSubtask(taskNewVersion)) {
            Subtask subtask = (Subtask) taskNewVersion;
            Epic epic = (Epic) listOfAllTasks.get(subtask.getEpicId());
            updateEpic(epic);
            listOfAllTasks.put(epic.getId(), epic);
        }
    }

    @Override
    public void deleteOneTask(int id) {    // удалить одну задачу
        if (!listOfAllTasks.containsKey(id))
            throw new TaskNotFoundException(String.format("The task with id=%d does not exist", id));
        Task task = listOfAllTasks.get(id);
        if (isEpic(task)) {
            for (Subtask subtask : ((Epic) task).getListOfSubtasks()) {
                if (hasTaskDurationAndTime(subtask))timeChecker.clearPeriod(subtask.getDuration(),
                        subtask.getStartTime());
                listOfAllTasks.remove(subtask.getId());
                prioritizedTasks.remove(subtask);
                if (inMemoryHistoryManager.contains(subtask)) {
                    inMemoryHistoryManager.remove(subtask.getId());
                }
            }
        } else if (isSubtask(task)) {
            Epic epic = (Epic) listOfAllTasks.get(((Subtask) task).getEpicId());
            epic.deleteSubtask((Subtask) task);
            epic.updateStatus();
            epic.updateDurationAndTime();
        }
        listOfAllTasks.remove(id);
        prioritizedTasks.remove(task);
        if (hasTaskDurationAndTime(task))timeChecker.clearPeriod(task.getDuration(), task.getStartTime());
        if (inMemoryHistoryManager.contains(task)) {
            inMemoryHistoryManager.remove(task.getId());
        }
    }
    // проверка периода задачи на пересечение с другими задачами, ранее добавленными в менеджер
    private void checkTimeAvailability (Duration duration, LocalDateTime startTime) {
        if (!timeChecker.checkTimeAvailability(duration, startTime))
            throw new TimeIntersectionException(String.format("The selected time is not available, the nearest "
                    + "available time is %s", Task.formatDate(timeChecker.getAvailableDateTime(duration, startTime))));
    }
    // обновить время и продолжительность задачи с удалением старой + добавлением новой информации во временной сетке
    private void updateTime (int id, Duration newDuration, LocalDateTime newStartTime) {
        Task taskOldVersion = listOfAllTasks.get(id);
        timeChecker.clearPeriod(taskOldVersion.getDuration(), taskOldVersion.getStartTime());
        checkTimeAvailability(newDuration, newStartTime);
    }
    // проверить заданы ли у задачи и время, и продолжительность
    private boolean hasTaskDurationAndTime(Task task) {
        return task.getDuration() != null && task.getStartTime() != null;
    }

    public boolean isSubtask(Task task) {    // проверка, является ли объект подзадачей
        if (task.getClass() == Subtask.class) return true;
        else return false;
    }

    public static boolean isEpic(Task task) {    // проверка, является ли объект эпиком
        if (task.getClass() == Epic.class) return true;
        else return false;
    }

    private void updateEpic(Epic epic){
        epic.updateStatus();
        epic.updateDurationAndTime();
    }
}
