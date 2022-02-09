package Managers;

import InMemoryManagers.InMemoryHistoryManager;
import InMemoryManagers.InMemoryTaskManager;

public class Managers {    // утилитарный класс для создания объекта менеджера задач

    public static TaskManager getDefault() {    // возвращает объект, реализующий интерфейс TaskManager
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {    // возвращает историю просмотра
        return new InMemoryHistoryManager();
    }
}
