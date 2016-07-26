package g0dkar.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.markdownj.MarkdownProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Lots of stuff to work with Strings
 * 
 * @author Rafael Lins
 *
 */
public class StringUtils {
	private static final Logger log = LoggerFactory.getLogger(StringUtils.class);
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/**
	 * Same as the JS one
	 * 
	 * @param urlComponent String to encode
	 * @return Encoded String
	 * @see URLEncoder
	 * @see #encodeURLComponent(String, String)
	 */
	public static String encodeURLComponent(final String urlComponent) {
		return encodeURLComponent(urlComponent, "UTF-8");
	}
	
	/**
	 * Same as the JS one, but with a user specified encoding ({@code UTF-8} is the recommended)
	 * 
	 * @param urlComponent String to encode
	 * @param encoding Encoding (recommended: {@code "UTF-8"})
	 * @return Encoded String
	 * @see URLEncoder
	 * @see URLEncoder#encode(String, String)
	 * @throws UnsupportedEncodingException Might be thrown by the {@link URLEncoder}
	 */
	public static String encodeURLComponent(final String urlComponent, final String encoding) {
		try {
			return URLEncoder.encode(urlComponent, encoding);
		} catch (final UnsupportedEncodingException e) {
			return urlComponent;
		}
	}
	
	/**
	 * Removes all HTML tags from the text. "&lt;b&gt;example&lt;/b&gt;" returns "example".
	 * 
	 * @param html Text with HTML tags.
	 * @return Text without tags.
	 * 
	 * @see Jsoup#clean(String, Whitelist)
	 * @see Whitelist#none()
	 */
	public static String stripHTML(final String html) {
		return html != null ? Jsoup.clean(html, Whitelist.none()) : null;
	}
	
	/**
	 * Remove most unsafe tags and attributes, leaving mostly format tags and links.
	 * 
	 * @param html HTML to be cleaned.
	 * @return Clean HTML.
	 * 
	 * @see Jsoup#clean(String, Whitelist)
	 * @see Whitelist#basic()
	 */
	public static String cleanHTML(final String html) {
		return html != null ? Jsoup.clean(html, Whitelist.basic()) : null;
	}
	
	/**
	 * Joins an {@code array} with a comma {@code ,} character
	 * 
	 * @param array The stuff to join
	 * @return "stuff,stuff,stuff"
	 */
	public static <T> String join(final T[] array) {
		return join(Arrays.asList(array));
	}
	
	/**
	 * Joins a {@link Collection} with a comma {@code ,} character
	 * 
	 * @param collection The stuff to join
	 * @return "stuff,stuff,stuff"
	 */
	public static String join(final Collection<?> collection) {
		return join(collection, ",");
	}
	
	/**
	 * Joins an {@code array} with a {@code separator}
	 * 
	 * @param array The stuff to join
	 * @param separator The separator
	 * 
	 * @return "stuff{@code [separator]}stuff{@code [separator]}stuff"
	 */
	public static <T> String join(final T[] array, final String separator) {
		return join(Arrays.asList(array), separator);
	}
	
	/**
	 * Joins a {@link Collection} with a {@code separator}
	 * 
	 * @param collection The stuff to join
	 * @param separator The separator
	 * @return "stuff{@code [separator]}stuff{@code [separator]}stuff"
	 */
	public static String join(final Collection<?> collection, final String separator) {
		if (collection != null) {
			final StringBuilder str = new StringBuilder();
			
			int i = 0;
			final int max = collection.size();
			for (final Object object : collection) {
				str.append(object.toString());
				
				if (++i < max) {
					str.append(separator);
				}
			}
			
			return str.toString();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Checks if a string is empty
	 * 
	 * @param string String to check
	 * @return {@code true} if the string is {@code null} or {@code ""}
	 */
	public static boolean isEmpty(final String string) {
		return string == null || string.length() == 0;
	}
	
	/**
	 * Checks if a string is blank ({@code null} or just spaces)
	 * 
	 * @param string String to check
	 * 
	 * @return {@code true} if it's {@code null}, {@code ""} or totally made out of spaces
	 */
	public static boolean isBlank(final String string) {
		return string == null || string.matches("\\s*");
	}
	
	/**
	 * Checks if the string is an Integer
	 * 
	 * @param string String to check
	 * @return {@code true} if it's made out of numbers
	 */
	public static boolean isInteger(final String string) {
		return string != null && string.matches("\\d+");
	}
	
	/**
	 * Checks if the string is an hexadecimal number
	 * 
	 * @param string String to check
	 * @return {@code true} if the string is made out of numbers and letters from {@code A} to {@code F} (case insensitive)
	 */
	public static boolean isNumberHexa(final String string) {
		return string != null && string.matches("[a-fA-F0-9]+");
	}
	
	/**
	 * Builds a URL query string from the {@link Map}
	 * 
	 * @param params Map to be serialized
	 * 
	 * @return The query string: {@code param=value&param=value&...}
	 * 
	 * @see URLEncoder
	 * @see URLEncoder#encode(String, String)
	 */
	public static String asURLParams(final Map<String, ?> params) {
		final StringBuilder str = new StringBuilder();
		
		if (params != null) {
			final Set<?> entrySet = params.entrySet();
			int i = 0;
			final int max = entrySet.size();
			for (final Object name : entrySet) {
				final Entry<String, ?> entry = (Entry<String, ?>) name;
				
				// null
				if (entry.getValue() == null) {
					str.append(entry.getKey());
					str.append("=");
				}
				// Is it a collection?
				else if (Collection.class.isAssignableFrom(entry.getValue().getClass())) {
					final Collection<?> c = (Collection<?>) entry.getValue();
					
					for (final Iterator<?> it = c.iterator(); it.hasNext();) {
						str.append(entry.getKey());
						str.append("=");
						str.append(encodeURLComponent(it.next().toString()));
						
						if (it.hasNext()) {
							str.append("&");
						}
					}
				}
				// Is it an array?
				else if (entry.getValue().getClass().isArray()) {
					final Object[] array = (Object[]) entry.getValue();
					
					for (final Object object : array) {
						str.append(entry.getKey());
						str.append("=");
						str.append(encodeURLComponent(object.toString()));
					}
				}
				// Just a simple parameter
				else {
					str.append(entry.getKey());
					str.append("=");
					str.append(encodeURLComponent(entry.getValue().toString()));
				}
				
				if (++i < max) {
					str.append("&");
				}
			}
		}
		
		return str.toString();
	}
	
	/**
	 * Reads the file and returns it as a String
	 * 
	 * @param file The file
	 * 
	 * @return Contents of the file as a String
	 * 
	 * @see FileInputStream
	 * @see #fromStream(InputStream)
	 */
	public static String fromFile(final File file) {
		try {
			return fromStream(new FileInputStream(file));
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads the {@link InputStream stream} as a single String
	 * 
	 * @param stream The Stream
	 * @return The stream content as a single String
	 */
	public static String fromStream(final InputStream stream) {
		return fromReader(new InputStreamReader(stream));
	}
	
	/**
	 * Reads a {@link Reader} and returns it as a String
	 * 
	 * @param reader The {@link Reader}
	 * @return The {@link Reader} contents as a single String
	 */
	public static String fromReader(final Reader reader) {
		final StringBuilder str = new StringBuilder();
		final BufferedReader br = new BufferedReader(reader);
		String line;
		final String newline = System.getProperty("line.separator");
		
		try {
			while ((line = br.readLine()) != null) {
				str.append(line);
				str.append(newline);
			}
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return str.toString();
	}
	
	/**
	 * Turns a string into a <em>slug</em> ({@code those-url-things-wordpress-has}, dunno if they're really called slugs or not)
	 * 
	 * @param str String to turn into a slug
	 * 
	 * @return {@code string-as-a-slug}
	 */
	public static String slug(final String str) {
		if (str != null) {
			return normalize(str).replaceAll("[\\p{Punct}\\s\\-]+", "-").replaceAll("-+$", "").replaceAll("-{2,}", "-").toLowerCase();
		}
		else {
			return null;
		}
	}
	
	/**
	 * Probably useful only to latin-based language speakers, like portuguese. It removes accents and other special stuff from letters.
	 * 
	 * In other words: this turns {@code ã} into {@code a} (by first turning {@code ã} into {@code a~} then stripping the {@code ~}).
	 * 
	 * @param string String to be normalized
	 * 
	 * @return Normalized string
	 */
	public static String normalize(final String string) {
		return string == null ? "" : Normalizer.normalize(string, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
	}
	
	/**
	 * Checks if the string is a representation of a {@link Boolean booleano} value: "{@code 0, false, falso, f, no, não, nao, n}"
	 * represents {@link Boolean#FALSE} ({@code não} = {@code no} in portuguese). Anything else is {@link Boolean#TRUE}.
	 * Case insensitive.
	 * 
	 * @param string String to check
	 * @return {@code true} or {@code false}
	 */
	public static boolean parseBoolean(final String string) {
		return (string != null) && !string.matches("(?i)0|f(als[eo])?|n([aã]?o)?");
	}
	
	/**
	 * Checks if the string is a representation of a {@link Boolean booleano} value: "{@code 0, false, falso, f, no, não, nao, n}"
	 * represents {@link Boolean#FALSE} ({@code não} = {@code no} in portuguese). Anything else is {@link Boolean#TRUE}.
	 * Case insensitive.
	 * 
	 * @param string String to check
	 * @param defaultValue What to return if {@code string} is {@code null}
	 * @return {@code true} ou {@code false}
	 */
	public static boolean parseBoolean(final String string, final boolean defaultValue) {
		return string == null ? defaultValue : !string.matches("(?i)0|f(als[eo])?|n([aã]?o)?");
	}
	
	/**
	 * Checks if the string is an Integer and converts it on base 10.
	 * 
	 * @param n The String
	 * @param defaultValue What to return if the string is {@code null} or isn't a number
	 * @return The parsed number or {@code defaultValue}
	 * 
	 * @see String#matches(String)
	 * @see Integer#parseInt(String, int)
	 */
	public static int parseInteger(final String n, final int defaultValue) {
		if (n != null && n.matches("\\d+")) {
			return Integer.parseInt(n, 10);
		}
		
		return defaultValue;
	}
	
	/**
	 * Checks if the string is an Integer and converts it as a {@link Long long} on base 10.
	 * 
	 * @param n The String
	 * @param defaultValue What to return if the string is {@code null} or isn't a number
	 * @return The parsed number or {@code defaultValue}
	 * 
	 * @see String#matches(String)
	 * @see Long#parseLong(String, int)
	 */
	public static long parseLong(final String n, final long defaultValue) {
		if (n != null && n.matches("\\d+")) {
			return Long.parseLong(n, 10);
		}
		
		return defaultValue;
	}
	
	/**
	 * Checks if the string is a Double and converts it.
	 * 
	 * @param n The String
	 * @param defaultValue What to return if the string is {@code null} or isn't a number
	 * @return The parsed number or {@code defaultValue}
	 * 
	 * @see String#matches(String)
	 * @see Double#parseDouble(String)
	 */
	public static double parseDouble(final String n, final double defaultValue) {
		if (n != null && n.matches("(\\d+)?\\.?\\d+")) {
			return Double.parseDouble(n);
		}
		
		return defaultValue;
	}
	
	/**
	 * Creates the MD5 hash for a given string.
	 * 
	 * @param data The string to be hashed
	 * @return The MD5 hash (lowercase)
	 */
	public static String md5(final String data) {
		final StringBuilder hash = new StringBuilder();
		
		try {
			final MessageDigest digester = MessageDigest.getInstance("MD5");
			final byte[] hashedData = digester.digest(data.getBytes());
			for (final byte b : hashedData) {
				hash.append(String.format("%02x", b));
			}
		} catch (final NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return hash.toString();
	}
	
	/**
	 * Downloads data from the specified URL and return it as a String
	 * 
	 * @param url The URL to {@code GET} data from
	 * @return Whatever text the URL returns
	 */
	public static String downloadAsString(final String url) {
		try {
			final URL downloadURL = new URL(url);
			final HttpURLConnection conn = (HttpURLConnection) downloadURL.openConnection();
			conn.setConnectTimeout(1000);
			if (conn.getResponseCode() == 200) {
				final StringBuffer str = new StringBuffer();
				final BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String line;
				
				while ((line = br.readLine()) != null) {
					str.append(line);
					str.append(LINE_SEPARATOR);
				}
				
				return str.toString();
			}
			else {
				return null;
			}
		} catch (final Exception e) {
			if (log.isErrorEnabled()) { log.error("Error while downloading " + url, e); }
//			else { e.printStackTrace(); }
			return null;
		}
	}
	
	/**
	 * Fully Capitalizes A String =P
	 * 
	 * @param string The string
	 * @return The Fully Capitalized String
	 */
	public static String capitalizeFully(final String string) {
		final StringBuilder str = new StringBuilder();
		final String[] words = string.split("\\s+");
		
		for (final String word : words) {
			str.append(word.substring(0, 1).toUpperCase());
			str.append(word.substring(1).toLowerCase());
		}
		
		return str.toString();
	}
	
	/**
	 * @param code The markdown code
	 * @return The markdown as HTML
	 */
	public static String markdown(final String code) {
		return new MarkdownProcessor().markdown(code);
	}
	
	/**
	 * @param code The markdown code
	 * @return Clears all Markdown from the text (it processes the markdown then strips ALL HTML)
	 */
	public static String clearMarkdown(final String code) {
		return Jsoup.clean(markdown(code), Whitelist.none());
	}
}
