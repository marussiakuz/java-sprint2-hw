public interface TaskManager {

    public void printAllTasks();    // вывести на экран строковое представление всех задач

    public void deleteAllTasks();    //удалить все задачи

    public void getTask(int id);    // получить информацию о задаче по её id

    public void addTask(Task task);    // добавить в список задачу

    public void updateTask(Task taskNewVersion);    // обновить задачу

    public void deleteOneTask(int id);    // удалить одну задачу
}
