package Tasks;

import java.util.ArrayList;

public class Epic extends Task {    // наследственный класс Эпик от класса Задача
    private ArrayList<Subtask> listOfSubtasks = new ArrayList<>();

    public Epic(String name, String description) {    // конструктор экземпляра класса Эпик
        super(name, description);
    }

    public ArrayList<Subtask> getListOfSubtasks() {    // получить коллекцию подзадач
        return listOfSubtasks;
    }

    public void addSubtask(Subtask subtask) {    // добавить подзадачу в коллекцию эпика
        this.listOfSubtasks.add(subtask);
    }

    public void deleteSubtask(Subtask subtask) {    // удалить подзадачу из колекции эпика
        this.listOfSubtasks.remove(subtask);
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации об эпике
        return "Epic{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", numberOfSubtasks=" + listOfSubtasks.size() + ", status=" + getStatus() + ", id=" + getId() + '}';
    }

    public StatusOfTask updateStatus() {    // вспомогательный метод для контроля над текущим статусом эпика
        int countStatusDone = 0;
        for(int i = 0; i < listOfSubtasks.size(); i++) {
            Subtask subtask = listOfSubtasks.get(i);
            if (subtask.getStatus().toString().equals("IN_PROGRESS")) {
                return StatusOfTask.IN_PROGRESS;
            } else if (subtask.getStatus().toString().equals("DONE")) {
                ++countStatusDone;
            }
        }
        if (countStatusDone == listOfSubtasks.size() && listOfSubtasks.size()!=0) return StatusOfTask.DONE;
        else return StatusOfTask.NEW;
    }
}

