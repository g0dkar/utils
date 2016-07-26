package g0dkar.utils.json;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.TemporalQueries;
import java.util.Calendar;
import java.util.TimeZone;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@link TypeAdapter} for GSON so it can nicely work with {@link Calendar} types (and ISO, which is MUCH easier to deal when using JS)
 * 
 * @author Rafael Lins
 * @see Instant
 */
public class GsonCalendarConverter extends TypeAdapter<Calendar> {
	public void write(final JsonWriter out, final Calendar value) throws IOException {
		out.value(value != null ? Instant.ofEpochMilli(value.getTimeInMillis()).atZone(ZoneId.of(value.getTimeZone().getID())).toString() : null);
	}
	
	public Calendar read(final JsonReader in) throws IOException {
		final Instant instant = Instant.parse(in.nextString());
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(instant.toEpochMilli());
		calendar.setTimeZone(TimeZone.getTimeZone(instant.query(TemporalQueries.zoneId())));
		return calendar;
	}
}
