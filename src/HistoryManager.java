import java.util.List;

public interface HistoryManager {    // интерфейс для управления историей просмотров

    public void add(Task task);    // добавить задачу в историю просмотра

    public List<Task> getHistory();    // получить список просмотренных задач
}
