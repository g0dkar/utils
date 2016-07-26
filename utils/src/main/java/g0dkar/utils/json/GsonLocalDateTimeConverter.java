package g0dkar.utils.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

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
public class GsonLocalDateTimeConverter extends TypeAdapter<LocalDateTime> {
	@Override
	public void write(final JsonWriter out, final LocalDateTime value) throws IOException {
		out.value(value != null ? value.toString() : null);
	}
	
	@Override
	public LocalDateTime read(final JsonReader in) throws IOException {
		return LocalDateTime.parse(in.nextString());
	}
}
