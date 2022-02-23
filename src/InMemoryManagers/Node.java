package InMemoryManagers;

public class Node<Task extends Tasks.Task> {    // узел связного списка
    private Task task;
    private Node<Task> next;
    private Node<Task> prev;

    Node(Node<Task> prev, Task task, Node<Task> next) {    // конструктор узла
        this.task = task;
        this.next = next;
        this.prev = prev;
    }

    public Task getTask() {    // вернуть задачу из узла
        return task;
    }

    public Node<Task> getNext() {    // вернуть узел, следующий за текущим
        return next;
    }

    public void setNext(Node<Task> next) {    // установить узел, следующий за текущим
        this.next = next;
    }

    public Node<Task> getPrev() {    // вернуть узел, предшествующий текущему
        return prev;
    }

    public void setPrev(Node<Task> prev) {    // установить узел, предшествующий текущему
        this.prev = prev;
    }
}
