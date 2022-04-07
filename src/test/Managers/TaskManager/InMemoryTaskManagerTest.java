package Managers.TaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @Override
    public TaskManager setManager() {
        return new InMemoryTaskManager();
    }
}
