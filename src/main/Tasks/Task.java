package Tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task implements Comparable<Task> {    // родительский класс Задача
    private final String name;
    private final String description;
    private static int count = 0;
    private int id;
    private StatusOfTask status;
    private Duration duration;
    private LocalDateTime startTime;
    private static TimeIntersectionChecker intersectionChecker = new TimeIntersectionChecker();

    public Task(String name, String description) {    // конструктор экземпляра класса Задача
        this.name = name;
        this.description = description;
        this.status = StatusOfTask.NEW;
        id = ++count;
    }

    public Task(String name, String description, Duration duration, LocalDateTime startTime) {    // конструктор экземпляра класса Задача
        this.name = name;
        this.description = description;
        this.status = StatusOfTask.NEW;
        setDurationAndStartTime(duration, startTime);
        id = ++count;
    }

    public int getId() {    // получить id номер задачи
        return id;
    }

    public void setId(int id) {    // установить id номер
        this.id = id;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDurationAndStartTime (Duration duration, LocalDateTime startTime) {
        this.duration = duration;
        if (startTime == null) return;
        if (intersectionChecker.checkTimeAvailability(duration, startTime)) this.startTime = startTime;
        else throw new TimeIntersectionException(String.format("The selected time is not available, " +
            "the nearest available time is %s", formatDate(intersectionChecker
            .getAvailableDateTime(duration, startTime))));
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) return null;
        return startTime.plus(duration);
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

    public TypeOfTask getType() {    // получить строковое обозначение типа задачи
        return TypeOfTask.TASK;
    }

    public String formatDuration() {
        if (duration == null) return "not set";
        int days = (int) duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        StringBuilder result = new StringBuilder();
        if (minutes != 0) result.append(minutes + " minutes");
        if (hours != 0) {
            if (hours == 1) result.insert(0, hours + " hour ");
            else result.insert(0, hours + " hours ");
        }
        if (days != 0) {
            if (days == 1) result.insert(0, days + " day ");
            else result.insert(0, days + " days ");
        }
        return result.toString().trim();
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "not set";
        final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatOfDate);
    }

    @Override
    public String toString() {    // переопределили метод для строкового представления информации о задаче
        return "Task{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", status="
            + status + '\'' + ", duration=" + formatDuration() + ", start=" + formatDate(getStartTime()) + ", id="
            + id + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(getClass() == o.getClass())) return false;
        Task task = (Task) o;
        if (id != task.id || status != task.status) return false;
        if (!Objects.equals(duration, task.duration) || !Objects.equals(startTime, task.startTime)) return false;
        return Objects.equals(name, task.name) && Objects.equals(description, task.description);
    }

    @Override
    public int hashCode() {
        int result = id + status.toString().hashCode();
        result = 31 * result + (name == null? 0 : name.hashCode()) + (description == null? 0 : description.hashCode());
        result = 31 * result + (duration == null? 0 : (int) duration.toMinutes())
                + (startTime == null? 0 : (startTime.getYear() + startTime.getMonthValue() + startTime.getDayOfMonth()
                + startTime.getHour() + startTime.getMinute()));
        return result;
    }

    @Override
    public int compareTo(Task task) {
        if (startTime == null || task.startTime == null) {
            return startTime == null && task.startTime == null? id - task.id : startTime == null? 1 : -1;
        }
        if (startTime.isEqual(task.startTime)) return id - task.id;
        return startTime.compareTo(task.startTime);
    }
}

