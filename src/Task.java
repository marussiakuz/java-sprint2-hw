public class Task {    // родительский класс Задача
    private final String name;
    private final String description;
    private String status;
    private static int count = 0;
    private final int id;
    public static final String statusIsNew = "NEW";
    public static final String statusIsInProgress = "IN_PROGRESS";
    public static final String statusIsDONE = "DONE";

    public Task(String name, String description) {    // конструктор экземпляра класса Задача
        this.name = name;
        this.description = description;
        this.status = statusIsNew;
        id = ++count;
    }

    public int getId() {    // получить id номер задачи
        return id;
    }

    public String getStatus() {    // получить строковое обозначение статуса задачи
        return status;
    }

    public void setStatus(String status) {    // установить статус
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

