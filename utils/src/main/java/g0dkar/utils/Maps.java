package g0dkar.utils;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Makes it less painful to work/deal with {@link Map Maps} in Java
 * 
 * @author Rafael Lins
 *
 */
public class Maps {
	private static final Logger log = LoggerFactory.getLogger(Maps.class);
	
	private final Map<String, Object> map;
	
	private Maps(final Map<String, Object> map) {
		this.map = map;
	}
	
	public static Maps use(final Map<String, Object> map) {
		return new Maps(map);
	}
	
	/**
	 * Return something from the map.
	 * 
	 * @param path {@code dot.path.to.something}
	 * @return The configuration value or {@code null} if it doesn't exist
	 */
	public Object get(final String path) {
		final String[] parts = path.split("\\.");
		Map<String, Object> current = map;
		
		for (int i = 0, max = parts.length - 1; i < max; i++) {
			final Object o = current.get(parts[i]);
			if (o != null && o instanceof Map) {
				current = (Map<String, Object>) o;
			}
			else {
				current = null;
				break;
			}
		}
		
		final Object found = current != null ? current.get(parts[parts.length - 1]) : null;
		
		return found;
	}
	
	public <T> T get(final String path, final T defaultValue) {
		final Object value = get(path);
		try {
			return value != null ? (T) value : defaultValue;
		} catch (final RuntimeException e) {
			return defaultValue;
		}
	}
	
	public Maps put(final String path, final Object value) {
		return pathToMap(map, path, value);
	}
	
	public Maps putIfNotNull(final String path, final Object value) {
		if (value != null) { return pathToMap(map, path, value); }
		else { return this; }
	}
	
	/**
	 * Takes a {@code path.to.something.separated.by.dots} and set it into a {@link HashMap}. <strong>IT OVERWRITES WHATEVER VALUE IS ALREADY ON THE MAP</strong>.
	 * 
	 * @param path The path
	 * @param value The value
	 * @return The {@link HashMap}
	 */
	private Maps pathToMap(Object currentMap, final String path, final Object value) {
		if (currentMap == null || !(currentMap instanceof Map)) {
			currentMap = new HashMap<String, Object>();
		}
		
		final Map<String, Object> map = (Map<String, Object>) currentMap;
		final int indexOfDot = path.indexOf(".");
		final String currentPart = indexOfDot >= 0 ? path.substring(0, indexOfDot) : path;
		
		if (log.isDebugEnabled()) { log.debug("currentMap = {}, path = {}, indexOfDot = {}, currentPart = {}", map, path, indexOfDot, currentPart); }
		
		if (path.indexOf(".", indexOfDot) >= 0) {
			if (log.isDebugEnabled()) { log.debug("We still have levels to go. Invoking pathToMap(\"{}\", {})...", path.substring(currentPart.length() + 1), value); }
			map.put(currentPart, pathToMap(map.get(currentPart), path.substring(currentPart.length() + 1), value));
		}
		else {
			if (log.isDebugEnabled()) { log.debug("Reached the path's end. Setting {} = {}", currentPart, value); }
			map.put(currentPart, value);
		}
		
		return this;
	}
}
