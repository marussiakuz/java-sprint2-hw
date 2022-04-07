package Managers.TaskManager;

import Managers.HistoryManager.InMemoryHistoryManager;
import Tasks.*;
import Exceptions.TaskNotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {    // Менеджер задач в оперативной памяти
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();
    private TreeSet<Task> prioritizedTasks = new TreeSet<>();
    private InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();

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

    @Override
    public ArrayList<Task> getAllTasks() {    // получить список всех задач
        return (ArrayList<Task>) listOfAllTasks.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {    // удалить все задачи
        listOfAllTasks.clear();
        prioritizedTasks.clear();
        inMemoryHistoryManager.clear();
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
        listOfAllTasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        if (isSubtask(task)) {
            ((Subtask) task).getEpic().updateStatus();
            ((Subtask) task).getEpic().updateDurationAndTime();
        }
    }

    @Override
    public void updateTask(Task taskNewVersion) {    // обновить задачу
        if (!getAllTasks().contains(taskNewVersion))
            throw new TaskNotFoundException("The task has not been found in the manager's task list");
        listOfAllTasks.put(taskNewVersion.getId(), taskNewVersion);
        if (isSubtask(taskNewVersion))  {
            ((Subtask) taskNewVersion).getEpic().updateStatus();
            ((Subtask) taskNewVersion).getEpic().updateDurationAndTime();
        }
    }

    @Override
    public void deleteOneTask(int id) {    // удалить одну задачу
        if (!listOfAllTasks.containsKey(id))
            throw new TaskNotFoundException(String.format("The task with id=%d does not exist", id));
        Task task = listOfAllTasks.get(id);
        if (isEpic(task)) {
            for (Subtask subtask : ((Epic) task).getListOfSubtasks()) {
                listOfAllTasks.remove(subtask.getId());
                if (inMemoryHistoryManager.contains(subtask)) {
                    inMemoryHistoryManager.remove(subtask.getId());
                }
            }
        } else if (isSubtask(task)) {
            Epic epic = ((Subtask) task).getEpic();
            epic.deleteSubtask((Subtask) task);
            epic.updateStatus();
            epic.updateDurationAndTime();
        }
        listOfAllTasks.remove(id);
        if (inMemoryHistoryManager.contains(task)) {
            inMemoryHistoryManager.remove(task.getId());
        }
    }

    public boolean isSubtask(Task task) {    // проверка, является ли объект подзадачей
        if (task.getClass() == Subtask.class) return true;
        else return false;
    }

    public boolean isEpic(Task task) {    // проверка, является ли объект эпиком
        if (task.getClass() == Epic.class) return true;
        else return false;
    }

    public static void main(String[] args) {    // проверка хранения приоритизированных задач по времени
        InMemoryTaskManager manager = new InMemoryTaskManager();

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

        manager.addTask(epic1);    // добавляем задачи в Менеджер с функцией автосохранения
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(subtask3);
        manager.addTask(epic2);
        manager.addTask(subtask4);
        manager.addTask(subtask5);
        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);
        // выводим на печать список в порядке удаления по времени
        System.out.println(manager.getPrioritizedTasks());
    }
}
