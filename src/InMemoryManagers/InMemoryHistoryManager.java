package InMemoryManagers;

import Managers.HistoryManager;
import Tasks.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {    // Менеджер просмотренных задач
    private Map<Integer, Node> viewedTasks = new HashMap<>();    // вспомогательная хеш-карта с узлами и id задач
    private Node<Task> head;
    private Node<Task> tail;

    void linkLast(Task task) {    // добавить задачу в конец связанного списка и во вспомогательную хэш-карту
        final Node<Task> previous = tail;
        final Node<Task> newNode = new Node<>(previous, task, null);
        tail = newNode;
        if (previous == null) head = newNode;
        else previous.setNext(newNode);
        viewedTasks.put(task.getId(), newNode);
    }

    public List<Task> getTasks() {    // преобразовать связанный список в ArrayList и вернуть его
        List<Task> lastViewedTasks = new ArrayList<>();
        Node prevNode = tail;
        while (prevNode != null) {
            lastViewedTasks.add(prevNode.getTask());
            prevNode = prevNode.getPrev();
        }
        return lastViewedTasks;
    }

    public void removeNode(Node<Task> node) {    // удалить узел из связанного списка
        final Node<Task> previous = node.getPrev();
        final Node<Task> next = node.getNext();
        if (node == head && node == tail) {
            head = null;
        } else if (node == tail && node != head) {
            previous.setNext(null);
            tail = previous;
        } else if (node == head && node != tail) {
            next.setPrev(null);
            head = next;
        } else {
            previous.setNext(next);
            next.setPrev(previous);
        }
    }

    @Override
    public void remove(int id) {    // удалить задачу из списка просмотренных по её id
        removeNode(viewedTasks.get(id));
        viewedTasks.remove(id);
    }

    @Override
    public void add(Task task) {    // добавить задачу в список просмотренных
        if (viewedTasks.containsKey(task.getId())) {
            remove(task.getId());
        }
        linkLast(task);
    }

    @Override
    public List<Task> getHistory() {    // получить лимитированный список просмотренных задач (лимит - 10 задач)
        ArrayList<Task> lastViewedTasks = new ArrayList<>();
        if (getTasks().size() <= 10) {
            lastViewedTasks.addAll(getTasks());
        } else {
            for (int i = 0; i < 10; i++) {
                lastViewedTasks.add(getTasks().get(i));
            }
        }
        return lastViewedTasks;
    }

    public void clear() {    // очистить список просмотренных задач
        head = null;
        tail = null;
        viewedTasks.clear();
    }

    public boolean contains(Task task) {    // есть ли в списке просмотренных определенная задача
        if (viewedTasks.containsKey(task.getId())) return true;
        else return false;
    }
}
