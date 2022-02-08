import java.util.*;
import java.util.Map.Entry;

public class InMemoryTaskManager implements TaskManager {    // Менеджер задач в оперативной памяти
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();
    private InMemoryHistoryManager inMemoryHistoryManager = new InMemoryHistoryManager();

    public List<Task> history() {    // получить список просмотренных задач
        return inMemoryHistoryManager.getHistory();
    }

    @Override
    public void printAllTasks() {    // вывести на экран строковое представление всех задач
        for (Entry entry : listOfAllTasks.entrySet()) {
            Task task = (Task) entry.getValue();
            getClassAndPrint(task);
        }
    }

    @Override
    public void deleteAllTasks() {    // удалить все задачи
        listOfAllTasks.clear();
        inMemoryHistoryManager.clear();
    }

    @Override
    public void getTask(int id) {    // получить информацию о задаче по её id
        Task task = listOfAllTasks.get(id);
        getClassAndPrint(task);
        inMemoryHistoryManager.add(task);
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
                while (inMemoryHistoryManager.contains(subtask)) {
                    inMemoryHistoryManager.remove(subtask);
                }
            }
        } else if (isSubtask(task)) {
            Epic epic = ((Subtask) task).getEpic();
            epic.deleteSubtask((Subtask) task);
            updateEpicStatus(epic);
        }
        listOfAllTasks.remove(id);
        while (inMemoryHistoryManager.contains(task)) {
            inMemoryHistoryManager.remove(task);
        }
    }

    public void getClassAndPrint(Task unknownTask) {    // определить класс объекта и вывести строковое представление
        if (isEpic(unknownTask)) {
            Epic epic = (Epic) unknownTask;
            System.out.println(epic);
        } else if (isSubtask(unknownTask)) {
            Subtask subtask = (Subtask) unknownTask;
            System.out.println(subtask);
        } else {
            Task task = (Task) unknownTask;
            System.out.println(task);
        }
    }

    public void getListOfSubtasks(Epic epic) {    // получить список подзадач определенного эпика
        for (Subtask subtask : epic.getListOfSubtasks()) {
            System.out.println(subtask);
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

    public void updateEpicStatus(Epic epic) {    // добавочный метод с целью обновления статуса эпика
        if (!epic.getStatus().equals(epic.updateStatus())) {
            epic.setStatus(epic.updateStatus());
        }
    }
}
