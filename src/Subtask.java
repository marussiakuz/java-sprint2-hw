public class Subtask extends Task {   // наследственный класс Подзадача от класса Задача
    private Epic epic;

    public Subtask(String name, String description, String status, Epic epic) { // конструктор экземпляра класса Подзадача
        super(name, description, status);
        this.epic = epic;
    }

    public Epic getEpic() {       // получить эпик, к которому относится подзадача
        return epic;
    }
}

