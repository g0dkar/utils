package g0dkar.utils.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

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
public class GsonLocalDateConverter extends TypeAdapter<LocalDate> {
	@Override
	public void write(final JsonWriter out, final LocalDate value) throws IOException {
		out.value(value != null ? value.toString() : null);
	}
	
	@Override
	public LocalDate read(final JsonReader in) throws IOException {
		return LocalDate.parse(in.nextString());
	}
}
