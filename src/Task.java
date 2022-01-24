public class Task {        // родительский класс Задача
    protected final String name;
    protected final String description;
    private String status;
    protected static int count = 0;
    private int id;

    public Task(String name, String description, String status) {    // конструктор экземпляра класса Задача
        this.name = name;
        this.description = description;
        if (status.equals("NEW") || status.equals("IN_PROGRESS") || status.equals("DONE")) {
            this.status = status;
        }
        id = ++count;
    }

    public int getId() {     // получить id номер задачи
        return id;
    }

    public String getStatus() {  // получить строковое обозначение статуса задачи
        return status;
    }

    public void setStatus(String status) {      // установить статус
        this.status = status;
    }
}

