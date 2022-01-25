import java.util.HashMap;
import java.util.Map.Entry;

public class Manager {    // Класс, в котором происходит управление состоянием задач
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();

    public void printAllTasks() {    // вывести на экран строковое представление всех задач
        for (Entry entry : listOfAllTasks.entrySet()) {
            Object object = entry.getValue();
            getClassAndPrint(object);
        }
    }

    public void deleteAllTasks() {   //удалить все задачи
        listOfAllTasks.clear();
    }

    public void getTask(int id) {    // получить информацию о задаче по её id
        Object object = listOfAllTasks.get(id);
        getClassAndPrint(object);
    }

    public void getClassAndPrint(Object object) {    // определить класс объекта и вывести строковое представление
        if (isEpic(object)) {
            Epic epic = (Epic) object;
            System.out.println(epic);
        } else if (isSubtask(object)) {
            Subtask subtask = (Subtask) object;
            System.out.println(subtask);
        } else {
            Task task = (Task) object;
            System.out.println(task);
        }
    }

    public void addTask(Task task) {    // добавить в список задачу
        listOfAllTasks.put(task.getId(), task);
        if (isSubtask(task)) updateEpicStatus(((Subtask) task).getEpic());
    }

    public void updateTask(Task taskNewVersion) {    // обновить задачу
        listOfAllTasks.put(taskNewVersion.getId(), taskNewVersion);
        if (isSubtask(taskNewVersion))  updateEpicStatus(((Subtask) taskNewVersion).getEpic());
    }

    public void deleteOneTask(int id) {    // удалить одну задачу
        Object task = listOfAllTasks.get(id);
        if (isEpic(task)) {
            for (Subtask subtask : ((Epic) task).getListOfSubtasks()) {
                listOfAllTasks.remove(subtask.getId());
            }
        } else if (isSubtask(task)) {
            Epic epic = ((Subtask) task).getEpic();
            epic.deleteSubtask((Subtask) task);
            updateEpicStatus(epic);
        }
        listOfAllTasks.remove(id);
    }

    public void getListOfSubtasks(Epic epic) {    // получить список подзадач определенного эпика
        for (Subtask subtask : epic.getListOfSubtasks()) {
            System.out.println(subtask);
        }
    }

    public boolean isSubtask(Object object) {    // проверка, является ли объект подзадачей
        if (object.getClass() == Subtask.class) return true;
        else return false;
    }

    public boolean isEpic(Object object) {    // проверка, является ли объект эпиком
        if (object.getClass() == Epic.class) return true;
        else return false;
    }

    public void updateEpicStatus(Epic epic) {    // добавочный метод с целью обновления статуса эпика
        if (!epic.getStatus().equals(epic.updateStatus())) {
            epic.setStatus(epic.updateStatus());
        }
    }
}
