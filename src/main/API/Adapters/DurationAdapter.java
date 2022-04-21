package API.Adapters;

import Managers.TaskManager.FileBackedTaskManager;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {    // Адаптер для чтения / записи Duration

    @Override
    public void write(final JsonWriter jsonWriter, final Duration duration) throws IOException {
        jsonWriter.value(formatDuration(duration));
    }

    @Override
    public Duration read(final JsonReader jsonReader) throws IOException {
        String duration = jsonReader.nextString();
        if (duration.equals("not set")) return null;
        return FileBackedTaskManager.durationFromString(duration);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "not set";

        int days = (int) duration.toDays();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();

        StringBuilder result = new StringBuilder();
        if (minutes != 0) result.append(minutes + " minutes");
        if (hours != 0) {
            if (hours == 1) result.insert(0, hours + " hour ");
            else result.insert(0, hours + " hours ");
        }
        if (days != 0) {
            if (days == 1) result.insert(0, days + " day ");
            else result.insert(0, days + " days ");
        }

        return result.toString().trim();
    }
}
