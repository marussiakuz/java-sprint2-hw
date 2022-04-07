package Managers.HistoryManager;

import Tasks.Task;
import java.util.List;

public interface HistoryManager {    // интерфейс для управления историей просмотров

    void add(Task task);    // добавить задачу в историю просмотра
    void remove(int id);    // удалить задачу по её id
    List<Task> getHistory();    // получить список просмотренных задач
}
