package Managers.HistoryManager;

import Tasks.Task;
import Exceptions.TaskNotFoundException;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {    // Менеджер просмотренных задач
    private Map<Integer, Node> viewedTasks = new HashMap<>();    // вспомогательная хеш-карта с узлами и id задач
    private Node<Task> head;
    private Node<Task> tail;

    private void linkLast(Task task) {    // добавить задачу в конец связанного списка и во вспомогательную хэш-карту
        final Node<Task> previous = tail;
        final Node<Task> newNode = new Node<Task>(previous, task, null);
        tail = newNode;
        if (previous == null) head = newNode;
        else previous.setNext(newNode);
        viewedTasks.put(task.getId(), newNode);
    }

    private List<Task> getTasks() {    // преобразовать связанный список в ArrayList и вернуть его
        List<Task> lastViewedTasks = new ArrayList<>();
        Node prevNode = tail;
        while (prevNode != null) {
            lastViewedTasks.add(prevNode.getTask());
            prevNode = prevNode.getPrev();
        }
        return lastViewedTasks;
    }

    private void removeNode(Node<Task> node) {    // удалить узел из связанного списка
        final Node<Task> previous = node.getPrev();
        final Node<Task> next = node.getNext();
        if (node == head && node == tail) {
            head = null;
            tail = null;
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

    private void clearLinkedList() {    // удаляет все элементы из связанного списка
        head = null;
        tail = null;
    }

    @Override
    public void remove(int id) {    // удалить задачу из списка просмотренных по её id
        if (!viewedTasks.containsKey(id))
            throw new TaskNotFoundException(String.format("The task with id=%d does not exist", id));
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
    public List<Task> getHistory() {    // вернуть список просмотренных задач
        return getTasks();
    }

    public void clear() {    // очистить историю просмотренных задач
        clearLinkedList();
        viewedTasks.clear();
    }

    public boolean contains(Task task) {    // есть ли в списке просмотренных определенная задача
        if (viewedTasks.containsKey(task.getId())) return true;
        else return false;
    }
}
