package InMemoryManagers;

import Managers.TaskManager;
import Tasks.*;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {    // Менеджер задач в оперативной памяти
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();
    private InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();

    public List<Task> history() {    // получить список просмотренных задач
        return inMemoryHistoryManager.getHistory();
    }

    @Override
    public ArrayList<Task> getAllTasks() {    // получить список всех задач
        return (ArrayList<Task>) listOfAllTasks.values().stream().collect(Collectors.toList());
    }

    @Override
    public void deleteAllTasks() {    // удалить все задачи
        listOfAllTasks.clear();
        inMemoryHistoryManager.clear();
    }

    @Override
    public Task getTask(int id) {    // получить информацию о задаче по её id
        Task task = listOfAllTasks.get(id);
        inMemoryHistoryManager.add(task);
        return task;
    }

    @Override
    public void addTask(Task task) {    // добавить в список задачу
        listOfAllTasks.put(task.getId(), task);
        if (isSubtask(task)) updateEpicStatus(((Subtask) task).getEpic());
    }

    @Override
    public void updateTask(Task taskNewVersion) {    // обновить задачу
        listOfAllTasks.put(taskNewVersion.getId(), taskNewVersion);
        if (isSubtask(taskNewVersion))  updateEpicStatus(((Subtask) taskNewVersion).getEpic());
    }

    @Override
    public void deleteOneTask(int id) {    // удалить одну задачу
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
            updateEpicStatus(epic);
        }
        listOfAllTasks.remove(id);
        if (inMemoryHistoryManager.contains(task)) {
            inMemoryHistoryManager.remove(task.getId());
        }
    }

    public ArrayList<Subtask> getListOfSubtasks(Epic epic) {    // получить список подзадач определенного эпика
        return epic.getListOfSubtasks();
    }

    public boolean isSubtask(Task task) {    // проверка, является ли объект подзадачей
        if (task.getClass() == Subtask.class) return true;
        else return false;
    }

    public boolean isEpic(Task task) {    // проверка, является ли объект эпиком
        if (task.getClass() == Epic.class) return true;
        else return false;
    }

    public void updateEpicStatus(Epic epic) {    // добавочный метод с целью обновления статуса эпика
        if (!epic.getStatus().equals(epic.updateStatus())) {
            epic.setStatus(epic.updateStatus());
        }
    }
}
