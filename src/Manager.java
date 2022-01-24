import java.util.HashMap;
import java.util.Map.Entry;

public class Manager {   // Класс, в котором происходит управление состоянием задач
    private HashMap<Integer, Task> listOfAllTasks = new HashMap<>();

    public void printAllTasks() {    // вывести на экран все задачи
        for (Entry entry : listOfAllTasks.entrySet()) {
            if (entry.getValue().getClass() == Task.class) {
                Task task = (Task) entry.getValue();
                System.out.println("Задача: " + task.name + ", описание: " + task.description
                        + ", идентификационный номер задачи " + entry.getKey());
                System.out.println("Статус " + task.getStatus());
                System.out.println("");
            } else if (entry.getValue().getClass()==Epic.class) {
                Epic epic = (Epic) entry.getValue();
                System.out.println("Эпик: " + epic.name + ", описание: " + epic.description
                        + ", идентификационный номер эпика " + entry.getKey());
                System.out.println("Статус " + epic.getStatus());
                System.out.println("Список подзадач эпика: ");
                if (epic.listOfSubtasks.isEmpty()) System.out.println("В текущем эпике отсутсвуют подзадачи");
                for (int i = 0; i < epic.listOfSubtasks.size(); i++) {
                    Subtask subtask = epic.listOfSubtasks.get(i);
                    System.out.println(String.format("Подзадача №%d: " + subtask.name + ", описание: "
                            + subtask.description + ", идентификационный номер подзадачи " + subtask.getId(), i+1));
                    System.out.println("Статус " + subtask.getStatus());
                }
                System.out.println("");
            }
        }
        if (listOfAllTasks.isEmpty()) System.out.println("Список задач пуст");
    }

    public void deleteAllTasks() {   //удалить все задачи
        listOfAllTasks.clear();
    }

    public void getTask (int id) {   // получить информацию о задаче по её id
        Object object = listOfAllTasks.get(id);
        if (object.getClass() == Task.class) {
            Task task = (Task) object;
            System.out.println("Задача: " + task.name + ", описание: " + task.description +
                    ", идентификационный номер задачи " + task.getId());
        } else if (object instanceof Epic) {
            Epic epic = (Epic) object;
            System.out.println("Эпик: " + epic.name + ", описание: " + epic.description +
                    ", идентификационный номер задачи " + epic.getId());
        } else {
            Subtask subtask = (Subtask) object;
            System.out.println("Подзадача: " + subtask.name + ", описание: " + subtask.description +
                    ", идентификационный номер задачи " + subtask.getId());
        }
    }

    public void addTask (Task task) {        // добавить в список задачу
        listOfAllTasks.put(task.getId(), task);
        if (isSubtask(task))  updateEpicStatus(task);
    }

    public void updateTask (Task taskNewVersion, int id) {    // обновить задачу
        listOfAllTasks.put(id, taskNewVersion);
        if (isSubtask(taskNewVersion))  updateEpicStatus(taskNewVersion);
    }

    public void deleteOneTask (int id) {             // удалить одну задачу
        Object task = listOfAllTasks.get(id);
        if (task.getClass() == Epic.class) {
            ((Epic) task).deleteAllSubtasks();
        } else if (task.getClass() == Subtask.class) {
            Epic epic = ((Subtask) task).getEpic();
            epic.listOfSubtasks.remove((Subtask) task);
        }
        listOfAllTasks.remove(id);
    }

    public void getListOfSubtasks (Epic epic) {         // получить список подзадач определенного эпика
        int numberOfSubtask = 1;
        for (Subtask subtask : epic.listOfSubtasks) {
            System.out.println(String.format("Подзадача №%d: " + subtask.name + ", описание: " + subtask.description +
                    ", идентификационный номер подзадачи " + subtask.getId(), numberOfSubtask++));
            System.out.println("Статус " + subtask.getStatus());
            System.out.println("");
        }
    }

    public boolean isSubtask (Task task) {       // проверка, является ли задача подзадачей
        if (task.getClass() == Subtask.class) return true;
        else return false;
    }

    public void updateEpicStatus (Task task) {    // добавочный метод с целью обновления статуса эпика
        Subtask subtask = (Subtask) task;
        if (!subtask.getEpic().updateStatus().equals("NEW")) {
            subtask.getEpic().setStatus(subtask.getEpic().updateStatus());
        }
    }
}
