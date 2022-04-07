package Managers.HistoryManager;

import Tasks.Task;

public class Node<T> {    // узел связного списка
    private Task task;
    private Node<T> next;
    private Node<T> prev;

    Node(Node<T> prev, Task task, Node<T> next) {    // конструктор узла
        this.task = task;
        this.next = next;
        this.prev = prev;
    }

    public Task getTask() {    // вернуть задачу из узла
        return task;
    }

    public Node<T> getNext() {    // вернуть узел, следующий за текущим
        return next;
    }

    public void setNext(Node<T> next) {    // установить узел, следующий за текущим
        this.next = next;
    }

    public Node<T> getPrev() {    // вернуть узел, предшествующий текущему
        return prev;
    }

    public void setPrev(Node<T> prev) {    // установить узел, предшествующий текущему
        this.prev = prev;
    }
}
