package API.Adapters;

import Enums.StatusOfTask;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class StatusOfTaskAdapter extends TypeAdapter<StatusOfTask> {    // Адаптер для чтения / записи StatusOfTask

    @Override
    public void write(JsonWriter jsonWriter, StatusOfTask statusOfTask) throws IOException {
        jsonWriter.value(statusOfTask.toString());
    }

    @Override
    public StatusOfTask read(JsonReader jsonReader) throws IOException {
        return StatusOfTask.valueOf(jsonReader.nextString());
    }
}
