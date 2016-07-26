package g0dkar.utils.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * A {@link TypeAdapter} for GSON so it can nicely work with {@link Instant} types which natively uses ISO
 * 
 * @author Rafael Lins
 * @see Instant
 *
 */
public class GsonLocalTimeConverter extends TypeAdapter<LocalTime> {
	@Override
	public void write(final JsonWriter out, final LocalTime value) throws IOException {
		out.value(value != null ? value.toString() : null);
	}
	
	@Override
	public LocalTime read(final JsonReader in) throws IOException {
		return LocalTime.parse(in.nextString());
	}
}
