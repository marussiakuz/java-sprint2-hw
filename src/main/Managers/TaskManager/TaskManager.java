package Managers.TaskManager;

import Tasks.Task;
import java.util.ArrayList;

public interface TaskManager {

    public ArrayList<Task> getAllTasks();    // вывести на экран строковое представление всех задач

    public void deleteAllTasks();    //удалить все задачи

    public Task getTask(int id);    // получить информацию о задаче по её id

    public void addTask(Task task);    // добавить в список задачу

    public void updateTask(Task taskNewVersion);    // обновить задачу

    public void deleteOneTask(int id);    // удалить одну задачу
}
