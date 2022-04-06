package Managers;

import Managers.InMemoryTaskManager;
import Managers.TaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    public TaskManager setManager() {
        return new InMemoryTaskManager();
    }
}