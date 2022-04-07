package Tasks;

import Enums.*;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {    // наследственный класс Подзадача от класса Задача
    private final Epic epic;

    public Subtask(String name, String description, Epic epic) {    // конструктор экземпляра класса Подзадача
        super(name, description);
        this.epic = epic;
        epic.addSubtask(this);
    }

    public Subtask(String name, String description, Epic epic, Duration duration, LocalDateTime startTime) {    // конструктор экземпляра класса Подзадача
        super(name, description, duration, startTime);
        this.epic = epic;
        epic.addSubtask(this);
    }

    public Epic getEpic() {    // получить эпик, к которому относится подзадача
        return epic;
    }

    public void setStatus(StatusOfTask status) {    // установить статус
        super.setStatus(status);
        epic.updateStatus();
    }
    // переопределенный метод сохранения длительности и старта задачи
    @Override
    public void setDurationAndStartTime(Duration duration, LocalDateTime startTime) {
        super.setDurationAndStartTime(duration, startTime);
        if (epic != null) getEpic().updateDurationAndTime();
    }

    @Override
    public TypeOfTask getType() {    // получить строковое обозначение типа задачи
        return TypeOfTask.SUBTASK;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Subtask subtask = (Subtask) o;
        return getEpic().getId()==subtask.getEpic().getId();
    }

    @Override
    public int hashCode() {
        return super.hashCode() * 31 + (epic == null? 0 : epic.hashCode());
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации о подзадаче
        return "Subtask{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", epicName='" + epic.getName() + '\'' + ", status=" + getStatus() + ", duration=" + formatDuration()
            + ", start=" + formatDate(getStartTime()) + ", id=" + getId() + '}';
    }
}

