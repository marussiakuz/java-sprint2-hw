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
    public String toString() {    // переопределили метод для строкового представления информации о подзадаче
        return "Subtask{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", epicName='" + epic.getName() + '\'' + ", status=" + getStatus() + ", id=" + getId() + '}';
    }
}

