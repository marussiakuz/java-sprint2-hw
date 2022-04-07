package Enums;

public enum TypeOfTask {    // перечисление типов задач
    TASK("TASK"),
    SUBTASK("SUBTASK"),
    EPIC("EPIC");

    private final String type;

    private TypeOfTask(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
