package Tasks;

import Enums.*;
import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {    // наследственный класс Подзадача от класса Задача
    private final int epicId;

    public Subtask(String name, String description, int epicId) {    // конструктор экземпляра класса Подзадача
        super(name, description);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, int epicId, Duration duration, LocalDateTime startTime) {    // конструктор экземпляра класса Подзадача
        super(name, description, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, int epicId) {    // конструктор экземпляра класса Подзадача
        super(id, name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {    // получить id эпика, к которому относится подзадача
        return epicId;
    }

    public void setStatus(StatusOfTask status) {    // установить статус
        super.setStatus(status);
    }
    // переопределенный метод сохранения длительности и старта задачи
    @Override
    public void setDurationAndStartTime(Duration duration, LocalDateTime startTime) {
        super.setDurationAndStartTime(duration, startTime);
    }

    @Override
    public TypeOfTask getType() {    // получить строковое обозначение типа задачи
        return TypeOfTask.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return getEpicId()==subtask.getEpicId();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + epicId;
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации о подзадаче
        return "Subtask{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", epicId='" + epicId + '\'' + ", status=" + getStatus() + ", duration=" + formatDuration()
            + ", start=" + formatDate(getStartTime()) + ", id=" + getId() + '}';
    }
}

