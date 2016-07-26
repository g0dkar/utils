package g0dkar.utils.json;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * Utility methods for working with JSON. Currently implemented with {@link Gson Google Gson}
 * 
 * @author Rafael g0dkar
 *
 */
public class Json {
	/** {@link Gson} instance pre-built and used by this class. */
	public static final Gson GSON = new GsonBuilder()
			.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
			.registerTypeAdapter(Date.class, new GsonDateConverter())
			.registerTypeAdapter(Calendar.class, new GsonCalendarConverter())
			.registerTypeAdapter(Instant.class, new GsonInstantConverter())
			.create();
	
	/**
	 * Turns an Object into {@link Gson#toJson(Object) json} ignoring {@code static} and fields marked as {@code transient} (by explicitly
	 * including the keyword {@code transient} in their declaration)
	 * 
	 * @param object The object
	 * @return The Json of it
	 * @see Gson#toJson(Object)
	 */
	public static String toJson(final Object object) {
		return GSON.toJson(object);
	}
	
	/**
	 * {@link Gson#fromJson(String, Class) Turns} a Json string back to an object
	 * @param json The Json String
	 * @param klass The final Object {@link Class}
	 * @return The object built from the Json Map
	 * @throws JsonParseException Invalid/malformed Json
	 */
	public static <T> T fromJson(final String json, final Class<T> klass) {
		return GSON.fromJson(json, klass);
	}
	
	/**
	 * Turns a Json into a {@link Map}
	 * @param json The Json
	 * @return The Map
	 */
	public static Map<String, Object> asJsonMap(final String json) {
		return GSON.fromJson(json, Map.class);
	}
	
	/**
	 * Builds a Json Map for a {@link Throwable} object.
	 * @param throwable The Throwable
	 * @return A Json Map with the {@link Throwable} class name and it's {@link Throwable#getMessage() message}.
	 */
	public static Map<String, Object> asJsonMap(final Throwable throwable) {
		return asJsonMap(throwable, false);
	}
	
	/**
	 * Builds a Json Map for a {@link Throwable} object. Optionally, recursively including it's {@link Throwable#getCause() stack trace}.
	 * @param throwable The Throwable
	 * @param withStacktrace Include the {@link Throwable#getCause() stack trace}?
	 * @return A Json Map with the {@link Throwable} class name and it's {@link Throwable#getMessage() message}.
	 */
	public static Map<String, Object> asJsonMap(final Throwable throwable, final boolean withStacktrace) {
		final Map<String, Object> json = new HashMap<>(withStacktrace && throwable.getCause() != null ? 3 : 2);
		json.put("exception", throwable.getClass().getName());
		json.put("message", throwable.getMessage());
		
		if (withStacktrace && throwable.getCause() != null) {
			json.put("cause", asJsonMap(throwable.getCause(), true));
		}
		
		return json;
	}
}
