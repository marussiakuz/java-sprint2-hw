package API.Adapters;

import Enums.StatusOfTask;
import Enums.TypeOfTask;
import Managers.TaskManager.FileBackedTaskManager;
import Tasks.Subtask;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubtaskAdapter extends TypeAdapter<Subtask> {    // Адаптер для чтения / записи Subtask
    private static final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final Subtask subtask) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("type");
        jsonWriter.value(subtask.getType().toString());
        jsonWriter.name("id");
        jsonWriter.value(String.valueOf(subtask.getId()));
        jsonWriter.name("name");
        jsonWriter.value(subtask.getName());
        jsonWriter.name("status");
        jsonWriter.value(subtask.getStatus().toString());
        jsonWriter.name("description");
        jsonWriter.value(subtask.getDescription());
        jsonWriter.name("duration");
        jsonWriter.value(subtask.formatDuration().equals("not set")? null : subtask.formatDuration());
        jsonWriter.name("start");
        jsonWriter.value(subtask.formatDate(subtask.getStartTime()));
        jsonWriter.name("epicId");
        jsonWriter.value(subtask.getEpicId());
        jsonWriter.endObject();
    }

    @Override
    public Subtask read(final JsonReader jsonReader) throws IOException {
        TypeOfTask type = TypeOfTask.SUBTASK;
        String name = null;
        String description = null;
        int id = -1;
        StatusOfTask status = StatusOfTask.NEW;
        Duration duration = null;
        LocalDateTime start = null;
        int epicId = -1;

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
                case "epicId" -> epicId = Integer.parseInt(jsonReader.nextString());
            }
        }
        jsonReader.endObject();

        Subtask subtask = new Subtask(id, name, description, epicId);
        if (duration != null) subtask.setDuration(duration);
        if (start != null) subtask.setStartTime(start);
        subtask.setStatus(status);
        return subtask;
    }
}
