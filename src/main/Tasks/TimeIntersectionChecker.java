package Tasks;

import java.time.*;
import java.util.TreeMap;

public class TimeIntersectionChecker {    // Класс для проверки на пересечение по времени
    private TreeMap<LocalDateTime, Boolean> periodChecker = new TreeMap<>();
    private final Period period = Period.ofYears(1);
    private final int minutes = 15;
    private LocalDateTime startLocalDateTime;
    private LocalDateTime endLocalDateTime;

    public TimeIntersectionChecker () {    // конструктор для заполнения мапы интеревалами времени по 15 мин
        startLocalDateTime = setStartLocalDateTime();
        endLocalDateTime = startLocalDateTime.plus(period);
        while (startLocalDateTime.isBefore(endLocalDateTime)) {
            periodChecker.put(startLocalDateTime, Boolean.TRUE);
            startLocalDateTime = startLocalDateTime.plusMinutes(minutes);
        }
    }
    // проверить не занято ли время от начала до конца
    public boolean checkTimeAvailability (Duration duration, LocalDateTime startDateTime) {
        LocalDateTime start = periodChecker.floorKey(startDateTime);
        if (!periodChecker.get(start)) return false;
        return start.isEqual(getTheFirstSuitableDate(duration, startDateTime));
    }
    // получить доступное время
    public LocalDateTime getAvailableDateTime (Duration duration, LocalDateTime startDateTime) {
        return getTheFirstSuitableDate(duration, startDateTime);
    }
    /*
    возвращает ближайшее от старта свободное время с доступной длительностью
    (если совпадает с нужным временем, меняет true на false )
    */
    private LocalDateTime getTheFirstSuitableDate (Duration duration, LocalDateTime startDateTime) {
        LocalDateTime start = periodChecker.floorKey(startDateTime);
        LocalDateTime finish = periodChecker.floorKey(startDateTime.plus(duration));
        if (startDateTime.isEqual(start)) finish = finish.minus(Duration.ofMinutes(minutes));
        TreeMap<LocalDateTime, Boolean> timeForTheTask = new TreeMap<>();
        LocalDateTime current = start;
        while (periodChecker.get(current) && !current.isAfter(finish)) {
            timeForTheTask.put(current, false);
            current = current.plusMinutes(minutes);
        }
        if (current.isAfter(finish)) {
            periodChecker.putAll(timeForTheTask);
            return start;
        }
        int count = periodChecker.subMap(start, finish.plusMinutes(minutes)).size();
        int i = 0;
        start = current.plusMinutes(minutes);
        while (i < count) {
            current = current.plusMinutes(minutes);
            if (!periodChecker.get(current)) {
                i = 0;
                start = current.plusMinutes(minutes);
            }
            i++;
        }
        return start;
    }
    // установить начало отсчета для временной сетки
    private LocalDateTime setStartLocalDateTime () {
        LocalDateTime start = LocalDateTime.now().plusMinutes(minutes);
        while (start.getMinute() % minutes != 0) {
            start = start.plusMinutes(1);
        }
        LocalTime time = LocalTime.of(start.getHour(), start.getMinute());
        start = LocalDateTime.of(start.toLocalDate(), time);
        return start;
    }
    // создать новую не заполненную временную сетку
    public TimeIntersectionChecker updateTimeIntersectionChecker (){
        periodChecker.clear();
        return new TimeIntersectionChecker();
    }
    // очистить ранее заполненный период
    public void clearPeriod(Duration duration, LocalDateTime dateTime) {
        LocalDateTime start = periodChecker.floorKey(dateTime);
        LocalDateTime finish = periodChecker.floorKey(dateTime.plus(duration));
        if (dateTime.isEqual(start)) finish = finish.minus(Duration.ofMinutes(minutes));
        LocalDateTime current = start;
        while (!current.isAfter(finish)) {
            periodChecker.put(current, true);
            current = current.plusMinutes(minutes);
        }
    }
}
