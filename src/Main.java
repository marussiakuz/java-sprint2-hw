public class Main {    // тестирование Менеджера задач
    public static void main(String[] args) {
        Epic epic1 = new Epic("Проект второго спринта", "Менеджер задач");
        Subtask subtask1 = new Subtask("Реализация", "создать проект Менеджер задач согласно ТЗ",
            epic1);
        Subtask subtask2 = new Subtask("Тестирование", "пройти тестирование", epic1);
        epic1.addSubtask(subtask1);
        epic1.addSubtask(subtask2);

        Epic epic2 = new Epic("Сдача проекта второго спринта", "Менеджер задач");
        Subtask subtask = new Subtask("Внесение исправлений", "Внести все исправления согласно ревью",
            epic2);
        epic2.addSubtask(subtask);

        Task task = new Task("файл README", "указать описание в файле README");

        Manager manager = new Manager();
        manager.addTask(epic1);
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(epic2);
        manager.addTask(subtask);
        manager.addTask(task);    // добавили все задачи
        manager.printAllTasks();    // распечатали список задач
        System.out.println("____________________________________________________________________");

        subtask1.setStatus(Task.statusIsInProgress);  // поменяли статус подзадачи эпика epic1
        manager.updateTask(subtask1);    // обновили подзадачу в Менеджере
        manager.printAllTasks();    // снова распечатали все задачи
        System.out.println("____________________________________________________________________");

        subtask1.setStatus(Task.statusIsDONE);
        subtask2.setStatus(Task.statusIsDONE);    // поменяли статус обоих подзадач эпика epic1
        subtask.setStatus(Task.statusIsInProgress);    // поменяли статус подзадачи эпика epic2
        manager.updateTask(subtask1);
        manager.updateTask(subtask2);
        manager.updateTask(subtask);    // обновили измененные подзадачи в Менеджере
        manager.printAllTasks();    // снова распечатали все задачи
        System.out.println("____________________________________________________________________");

        manager.deleteOneTask(5);    // удалили единственную подзадачу эпика epic2 со статусом "IN_PROGRESS"
        manager.printAllTasks();    // распечатали все задачи
        System.out.println("____________________________________________________________________");

        manager.getTask(6);    // получили информацию по id задачи
        System.out.println("____________________________________________________________________");

        manager.getListOfSubtasks(epic1);    // получили список подзадач эпика
        System.out.println("____________________________________________________________________");

        manager.deleteAllTasks();    // удалили все задачи
        manager.printAllTasks();    // распечатали список задач
        System.out.println("____________________________________________________________________");
    }
}

