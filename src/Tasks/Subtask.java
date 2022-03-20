package Tasks;

public class Subtask extends Task {    // наследственный класс Подзадача от класса Задача
    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {    // конструктор экземпляра класса Подзадача
        super(name, description);
        this.epic = epic;
    }

    public Epic getEpic() {    // получить эпик, к которому относится подзадача
        return epic;
    }

    @Override
    public TypeOfTask getType() {    // получить строковое обозначение типа задачи
        return TypeOfTask.SUBTASK;
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации о подзадаче
        return "Subtask{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", epicName='" + epic.getName() + '\'' + ", status=" + getStatus() + ", id=" + getId() + '}';
    }
}

