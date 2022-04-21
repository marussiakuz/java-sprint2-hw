package API.Adapters;

import Enums.StatusOfTask;
import Enums.TypeOfTask;
import Managers.TaskManager.FileBackedTaskManager;
import Tasks.Task;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.sql.Struct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskAdapter extends TypeAdapter<Task> {    // Адаптер для чтения / записи класса Task
    private static final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final Task task) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("type");
        jsonWriter.value(task.getType().toString());
        jsonWriter.name("id");
        jsonWriter.value(String.valueOf(task.getId()));
        jsonWriter.name("name");
        jsonWriter.value(task.getName());
        jsonWriter.name("status");
        jsonWriter.value(task.getStatus().toString());
        jsonWriter.name("description");
        jsonWriter.value(task.getDescription());
        jsonWriter.name("duration");
        jsonWriter.value(task.formatDuration().equals("not set")? null : task.formatDuration());
        jsonWriter.name("start");
        jsonWriter.value(task.formatDate(task.getStartTime()));
        jsonWriter.endObject();
    }

    @Override
    public Task read(final JsonReader jsonReader) throws IOException {
        TypeOfTask type = TypeOfTask.TASK;
        String name = null;
        String description = null;
        int id = -1;
        StatusOfTask status = StatusOfTask.NEW;
        Duration duration = null;
        LocalDateTime start = null;

        jsonReader.beginObject();
        String fieldName = null;
        while (jsonReader.hasNext()) {
            JsonToken token = jsonReader.peek();
            if (token.equals(JsonToken.NAME)) {
                fieldName = jsonReader.nextName();
            }
            token = jsonReader.peek();
            switch (fieldName) {
                case "type" -> type = TypeOfTask.valueOf(jsonReader.nextString());
                case "name" -> name = jsonReader.nextString();
                case "description" -> description = jsonReader.nextString();
                case "id" -> id = Integer.parseInt(jsonReader.nextString());
                case "status" -> status = StatusOfTask.valueOf(jsonReader.nextString());
                case "duration" -> {
                    String durationString = jsonReader.nextString();
                    if (durationString.equals("not set")) duration = null;
                    else duration = FileBackedTaskManager.durationFromString(durationString);
                }
                case "start" -> {
                    String localDateTimeString = jsonReader.nextString();
                    if (localDateTimeString.equals("not set")) start = null;
                    else start = LocalDateTime.parse(localDateTimeString, formatOfDate);
                }
            }
        }
        jsonReader.endObject();

        Task task = new Task(id, name, description);
        task.setStatus(status);
        if (duration != null) task.setDuration(duration);
        if (start != null) task.setStartTime(start);
        return task;
    }
}
