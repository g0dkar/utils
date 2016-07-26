package g0dkar.utils.json;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@link TypeAdapter} for GSON so it can nicely work with {@link Date} types (and ISO, which is MUCH easier to deal when using JS)
 * 
 * @author Rafael Lins
 * @see Instant
 */
public class GsonDateConverter extends TypeAdapter<Date> {
	@Override
	public void write(final JsonWriter out, final Date value) throws IOException {
		out.value(value != null ? Instant.ofEpochMilli(value.getTime()).atOffset(ZoneOffset.ofTotalSeconds(value.getTimezoneOffset() * 60)).toString() : null);
	}
	
	@Override
	public Date read(final JsonReader in) throws IOException {
		return new Date(Instant.parse(in.nextString()).toEpochMilli());
	}
}
