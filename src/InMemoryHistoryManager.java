import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {    // Менеджер просмотренных задач

    private ArrayDeque<Task> lastViewedTasks = new ArrayDeque<>();    // список просмотренных задач

    public void remove(Task task) {    // удалить задачу из списка просмотренных
        lastViewedTasks.remove(task);
    }

    public void clear() {    // очистить список просмотренных задач
        lastViewedTasks.clear();
    }

    public boolean contains(Task task) {    // есть ли в списке просмотренных определенная задача
        if (lastViewedTasks.contains(task)) return true;
        else return false;
    }

    @Override
    public void add(Task task) {    // добавить задачу в список просмотренных
        lastViewedTasks.addFirst(task);
    }

    @Override
    public List<Task> getHistory() {    // получить лимитированный список просмотренных задач (лимит - 10 задач)
        List <Task> limitLastViewedTasks = new ArrayList<>();
        if (lastViewedTasks.isEmpty()) return limitLastViewedTasks;
        ArrayDeque<Task> copy = lastViewedTasks.clone();
        int limit = 0;
        for (Task task : lastViewedTasks) {
            limitLastViewedTasks.add(copy.pop());
            limit++;
            if (limit == 10) break;
        }
        return limitLastViewedTasks;
    }
}
