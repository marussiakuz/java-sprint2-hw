package API.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

import Managers.TaskManager.HTTPTaskManager;
import com.sun.net.httpserver.HttpServer;

public class HttpTaskServer {    // сервер для маппинга запросов на методы менеджера HTTPTaskManager
    private static final int PORT = 8080;
    private HttpServer httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
    private HTTPTaskManager manager;

    public HttpTaskServer(HTTPTaskManager manager) throws IOException {
        this.manager = manager;
        httpServer.createContext("/tasks", new TasksHandler(manager));
    }

    public void start() {    // запуск сервера
        httpServer.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {    // остановка сервера
        httpServer.stop(0);
    }
}
