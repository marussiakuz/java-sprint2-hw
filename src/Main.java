public class Main {     // тестирование Менеджера задач
    public static void main(String[] args) {
        Epic epic1 = new Epic("Проект второго спринта", "Менеджер задач", "NEW");
        Subtask subtask1 = new Subtask("Реализация", "создать проект Менеджер задач согласно ТЗ",
                "NEW", epic1);
        Subtask subtask2 = new Subtask("Тестирование", "пройти тестирование", "NEW", epic1);
        epic1.listOfSubtasks.add(subtask1);
        epic1.listOfSubtasks.add(subtask2);

        Epic epic2 = new Epic("Сдача проекта второго спринта", "Менеджер задач", "NEW");
        Subtask subtask = new Subtask("Внесение исправлений", "Внести все исправления согласно ревью",
                "NEW", epic2);
        epic2.listOfSubtasks.add(subtask);

        Task task = new Task("файл README", "указать описание в файле README", "NEW");

        Manager manager = new Manager();
        manager.addTask(epic1);
        manager.addTask(subtask1);
        manager.addTask(subtask2);
        manager.addTask(epic2);
        manager.addTask(subtask);
        manager.addTask(task);    // добавили все задачи
        manager.printAllTasks();  // распечатали список задач
        System.out.println("____________________________________________________________________");
        System.out.println("");

        subtask1.setStatus("IN_PROGRESS");  // поменяли статус подзадачи эпика epic1
        manager.updateTask(subtask1, 2);   // обновили подзадачу в Менеджере
        manager.printAllTasks();   // снова распечатали все задачи
        System.out.println("____________________________________________________________________");
        System.out.println("");

        subtask1.setStatus("DONE");
        subtask2.setStatus("DONE");   // поменяли статус обоих подзадач эпика epic1
        manager.updateTask(subtask1, 2);
        manager.updateTask(subtask2, 3);  // обновили обе подзадачи в Менеджере
        manager.printAllTasks();   // снова распечатали все задачи
        System.out.println("____________________________________________________________________");
        System.out.println("");

        manager.getTask(6);    // получили информацию по id задачи
        System.out.println("____________________________________________________________________");
        System.out.println("");

        manager.getListOfSubtasks(epic1); // получили список подзадач эпика
        System.out.println("____________________________________________________________________");
        System.out.println("");

        manager.deleteOneTask(5); // удалили одну подзадачу по её id
        manager.printAllTasks();
        System.out.println("____________________________________________________________________");
        System.out.println("");

        manager.deleteAllTasks();  // удалили все задачи
        manager.printAllTasks();   // распечатали список задач

    }
}

