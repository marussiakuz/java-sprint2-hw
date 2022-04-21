package API.Adapters;

import Enums.StatusOfTask;
import Enums.TypeOfTask;
import Managers.TaskManager.FileBackedTaskManager;
import Tasks.Epic;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EpicAdapter extends TypeAdapter<Epic> {    // Адаптер для чтения / записи Epic
    private static final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final Epic epic) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("type");
        jsonWriter.value(epic.getType().toString());
        jsonWriter.name("id");
        jsonWriter.value(String.valueOf(epic.getId()));
        jsonWriter.name("name");
        jsonWriter.value(epic.getName());
        jsonWriter.name("status");
        jsonWriter.value(epic.getStatus().toString());
        jsonWriter.name("description");
        jsonWriter.value(epic.getDescription());
        jsonWriter.name("duration");
        jsonWriter.value(epic.formatDuration().equals("not set")? null : epic.formatDuration());
        jsonWriter.name("start");
        jsonWriter.value(epic.formatDate(epic.getStartTime()));
        jsonWriter.endObject();
    }

    @Override
    public Epic read(final JsonReader jsonReader) throws IOException {
        TypeOfTask type = TypeOfTask.EPIC;
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

        Epic epic = new Epic(id, name, description);
        if (duration != null) epic.setDuration(duration);
        if (start != null) epic.setStartTime(start);
        epic.setStatus(status);
        return epic;
    }
}
