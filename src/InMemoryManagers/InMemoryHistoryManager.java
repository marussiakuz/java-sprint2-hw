package InMemoryManagers;

import Managers.HistoryManager;
import Tasks.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {    // Менеджер просмотренных задач
    private List<Task> lastViewedTasks = new ArrayList<>();    // список просмотренных задач

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
        lastViewedTasks.add(task);
        if (lastViewedTasks.size()>10) {
            lastViewedTasks.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {    // получить лимитированный список просмотренных задач (лимит - 10 задач)
        return lastViewedTasks;
    }
}
