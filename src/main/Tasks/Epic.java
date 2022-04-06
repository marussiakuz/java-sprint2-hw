package Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {    // наследственный класс Эпик от класса Задача
    private List<Subtask> listOfSubtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description) {    // конструктор экземпляра класса Эпик
        super(name, description);
    }

    public List<Subtask> getListOfSubtasks() {    // получить коллекцию подзадач
        return listOfSubtasks;
    }

    public void addSubtask(Subtask subtask) {    // добавить подзадачу в коллекцию эпика
        listOfSubtasks.add(subtask);
        updateStatus();
        updateDurationAndTime();
    }

    public void deleteSubtask(Subtask subtask) {    // удалить подзадачу из колекции эпика
        if (!getListOfSubtasks().contains(subtask))
            throw new TaskNotFoundException(String.format("The subtask belongs to another epic. " +
                    "Call the method from an epic %s", subtask.getEpic().getName()));
        this.listOfSubtasks.remove(subtask);
        updateStatus();
        updateDurationAndTime();
    }

    public void updateDurationAndTime() {    // обновить длительность задачи, время старта и конца
        if (listOfSubtasks.isEmpty()) {
            setDuration(null);
            setStartTime(null);
            endTime = null;
            return;
        }
        endTime = null;
        LocalDateTime start = null;
        Duration totalDuration = null;
        for (Subtask subtask : listOfSubtasks) {
            if (totalDuration != null && subtask.getDuration() != null) {
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
            if (totalDuration == null && subtask.getDuration() != null) {
                totalDuration = subtask.getDuration();
            }
            if (subtask.getEndTime() != null) endTime = endTime == null? subtask.getEndTime()
                    : endTime.isAfter(subtask.getEndTime()) ? endTime : subtask.getEndTime();
            if (subtask.getStartTime() != null) start = start == null? subtask.getStartTime()
                    : start.isBefore(subtask.getStartTime()) ? start : subtask.getStartTime();
        }
        setDuration(totalDuration);
        setStartTime(start);
    }

    public StatusOfTask updateStatus() {    // вспомогательный метод для контроля над текущим статусом эпика
        int countStatusDone = 0;
        for (Subtask subtask : listOfSubtasks) {
            switch (subtask.getStatus()) {
                case IN_PROGRESS:
                    setStatus(StatusOfTask.IN_PROGRESS);
                    return StatusOfTask.IN_PROGRESS;
                case DONE: ++countStatusDone;
            }
        }
        if (countStatusDone == listOfSubtasks.size() && listOfSubtasks.size()!=0) {
            setStatus(StatusOfTask.DONE);
            return StatusOfTask.DONE;
        }
        setStatus(StatusOfTask.NEW);
        return StatusOfTask.NEW;
    }

    @Override
    public LocalDateTime getEndTime() {    // вернуть время окончания задачи
        return endTime;
    }

    @Override
    public TypeOfTask getType() {    // получить строковое обозначение типа задачи
        return TypeOfTask.EPIC;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return getListOfSubtasks().equals(epic.getListOfSubtasks());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode() * 31;
        for (Subtask subtask : listOfSubtasks) {
            result += 31 * subtask.getId();
        }
        return result;
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации об эпике
        return "Epic{" + "name='" + getName() + '\'' + ", description='" + getDescription() + '\''
            + ", numberOfSubtasks=" + listOfSubtasks.size() + ", status=" + getStatus() + ", duration="
            + formatDuration() + ", start=" + formatDate(getStartTime()) + ", id=" + getId() + '}';
    }
}


