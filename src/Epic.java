import java.util.ArrayList;

public class Epic extends Task{   // наследственный класс Эпик от класса Задача
    protected ArrayList<Subtask> listOfSubtasks = new ArrayList<>();

    public Epic(String name, String description, String status) {     // конструктор экземпляра класса Эпик
        super(name, description, status);
    }

    public String updateStatus(){       // вспомогательный метод для контроля над текущим статусом эпика
        int countStatusDone = 0;
        for(int i = 0; i<listOfSubtasks.size(); i++) {
            Subtask subtask = listOfSubtasks.get(i);
            if (subtask.getStatus().equals("IN_PROGRESS")) {
                return "IN_PROGRESS";
            } else if (subtask.getStatus().equals("DONE")) {
                ++countStatusDone;
            }
        }
        if (countStatusDone == listOfSubtasks.size()) return "DONE";
        else return "NEW";
    }

    public void deleteAllSubtasks(){    // удалить все подзадачи из списка подзадач эпика
        listOfSubtasks.clear();
    }
}


