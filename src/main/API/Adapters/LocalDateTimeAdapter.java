package API.Adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {    // Адаптер для чтения / записи LocalDateTime
    private static final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDate) throws IOException {
        jsonWriter.value(formatDate(localDate));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String localDateTime = jsonReader.nextString();
        if (localDateTime.equals("not set")) return null;
        return LocalDateTime.parse(localDateTime, formatOfDate);
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "not set";
        final DateTimeFormatter formatOfDate = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatOfDate);
    }
}
