public class Task {    // родительский класс Задача
    private final String name;
    private final String description;
    private static int count = 0;
    private final int id;
    private StatusOfTask status;

    public Task(String name, String description) {    // конструктор экземпляра класса Задача
        this.name = name;
        this.description = description;
        this.status = StatusOfTask.NEW;
        id = ++count;
    }

    public int getId() {    // получить id номер задачи
        return id;
    }

    public StatusOfTask getStatus() {    // получить строковое обозначение статуса задачи
        return status;
    }

    public void setStatus(StatusOfTask status) {    // установить статус
        this.status = status;
    }

    public String getName() {    // получить имя задачи
        return name;
    }

    public String getDescription() {    // получить описание задачи
        return description;
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации о задаче
        return "Task{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", status="
            + status + ", id=" + id + '}';
    }
}

