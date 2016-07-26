package g0dkar.utils.time;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

/**
 * <h1>Human Time</h1>
 * 
 * <p>Human Time: A class to show time for humans. It simply creates strings like "just now", "x minutes ago" and "x hours ago".</p>
 * 
 * <h2>How to use</h2>
 * 
 * <p>This was built to be used fluently and (preferably) as a {@link Singleton}. You can {@link Inject @Inject} it, or simply create a {@code new HumanTime()}
 * or even a {@code HumanTime.instance()}
 * After creating it,</p>
 * 
 * @author Rafael Lins - g0dkar
 *
 */
@Singleton
@Named("humanTime")
public class HumanTime {
	@Inject
	private Logger log;
	
	/**
	 * The time thresholds. A time threshold is a number that denotes that after that and before the next threshold, use that time.
	 * 
	 * Like "from 0 to 1500ms use {@code just now}"
	 */
	private List<HumanTimeThreshold> thresholds;
	
	/**
	 * Which {@link ResourceBundle} should be used to check for i18n strings? (default = {@link #DEFAULT_BUNDLE_NAME})
	 */
	@Inject
	private ResourceBundle resourceBundle;
	private static final String DEFAULT_BUNDLE_NAME = "messages";
	
	/**
	 * Which {@link Locale} should be used for l10n?
	 */
	@Inject
	private Locale locale;
	
	@PostConstruct
	private void dependencyInjectionInit() {
		if (log.isDebugEnabled()) { log.debug("Instantiated by Dependency Injection System. Doing post-construct stuff..."); }
		withDefaultThresholds();
	}
	
	/**
	 * Same as {@code withLocale(}{@link Locale#getDefault()}{@code )}
	 * @return {@code this}
	 */
	public HumanTime withDefaultLocale() {
		return withLocale(Locale.getDefault());
	}
	
	/**
	 * Sets which {@link Locale} should be used for L10N
	 * @param locale The {@link Locale} to use
	 * @return {@code this}
	 */
	public HumanTime withLocale(final Locale locale) {
		this.locale = locale;
		return this;
	}
	
	/**
	 * Same as {@code withDefaultResourceBundle(}{@link ResourceBundle#getBundle(String)}{@code )}
	 * @return {@code this}
	 */
	public HumanTime withDefaultResourceBundle() {
		return withResourceBundle(locale == null ? ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME) : ResourceBundle.getBundle(DEFAULT_BUNDLE_NAME, locale));
	}
	
	/**
	 * Sets which {@link ResourceBundle} should be used for I18N
	 * @param resourceBundle The {@link ResourceBundle} to use
	 * @return {@code this}
	 */
	public HumanTime withResourceBundle(final ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
		return this;
	}
	
	/**
	 * <p>Sets a set of default {@link HumanTimeThreshold thresholds} following this pattern:</p>
	 * <p><strong>NOTE:</strong> By default, if these are not found in the current {@link #withResourceBundle(ResourceBundle) resource bundle}
	 * an <strong>english</strong> locale hardcoded values will be used.</p>
	 * <table border="1" cellpadding="3" summary="message.properties properties that this class uses">
	 * <tr><th>Elapsed Time</th><th>Time Unit</th><th>{@link ResourceBundle} key</th><th>Default Text</th></tr>
	 * 
	 * <tr><td>{@link Long#MIN_VALUE The End of Time} to Now (future)</td><td>-</td><td>{@code humanTime.future}</td><td>the future!</td></tr>
	 * 
	 * <tr><td>0 - 20 seconds</td><td>Seconds</td><td>{@code humanTime.justNow}</td><td>just now</td></tr>
	 * <tr><td>20 - 60 seconds</td><td>Seconds</td><td>{@code humanTime.someSecondsAgo}</td><td>a few seconds ago</td></tr>
	 * 
	 * <tr><td>1 minute</td><td>Minutes</td><td>{@code humanTime.aMinuteAgo}</td><td>a minute ago</td></tr>
	 * <tr><td>2 - 60 minutes</td><td>Minutes</td><td>{@code humanTime.someMinutesAgo}</td><td>X minutes ago</td></tr>
	 * 
	 * <tr><td>1 hour</td><td>Hours</td><td>{@code humanTime.anHourAgo}</td><td>an hour ago</td></tr>
	 * <tr><td>2 - 24 hours</td><td>Hours</td><td>{@code humanTime.someHoursAgo}</td><td>X hours ago</td></tr>
	 * 
	 * <tr><td>1 day</td><td>Days</td><td>{@code humanTime.yesterday}</td><td>yesterday</td></tr>
	 * <tr><td>2 - 30 days</td><td>Days</td><td>{@code humanTime.someDaysAgo}</td><td>X days ago</td></tr>
	 * 
	 * <tr><td>1 month (= 30 days)</td><td>Months</td><td>{@code humanTime.aMinuteAgo}</td><td>last month</td></tr>
	 * <tr><td>2 - 12 months</td><td>Months</td><td>{@code humanTime.someMinutesAgo}</td><td>X months ago</td></tr>
	 * 
	 * <tr><td>1 year</td><td>Years</td><td>{@code humanTime.lastYear}</td><td>last year</td></tr>
	 * <tr><td>2+ years</td><td>Years</td><td>{@code humanTime.someYearsAgo}</td><td>X years ago</td></tr>
	 * 
	 * <tr><td>{@link Long#MAX_VALUE The Big Bang}</td><td>Years</td><td>{@code humanTime.bigBang}</td><td>right after the Big Bang</td></tr>
	 * </table>
	 */
	public HumanTime withDefaultThresholds() {
		final List<HumanTimeThreshold> thresholds = new ArrayList<HumanTimeThreshold>(14);
		
		// Future!
		thresholds.add(new HumanTimeThreshold(Long.MIN_VALUE, 0, "humanTime.future")); // Future! (timeDiff is negative)
		// Seconds
		thresholds.add(new HumanTimeThreshold(0, 1000, "humanTime.justNow")); // Right now
		thresholds.add(new HumanTimeThreshold(20 * 1000, 1000, "humanTime.aFewSecondsAgo")); // 20 seconds since now
		// Minutes
		thresholds.add(new HumanTimeThreshold(60 * 1000, 60 * 1000, "humanTime.aMinuteAgo")); // 1 minute since now
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 2, 60 * 1000, "humanTime.someMinutesAgo")); // 2+ minutes since now
		// Hours
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60, 60 * 1000 * 60, "humanTime.anHourAgo")); // 1 hour since now
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 2, 60 * 1000 * 60, "humanTime.someHoursAgo")); // 2+ hours since now
		// Days
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 30, 60 * 1000 * 60 * 24, "humanTime.yesterday")); // 1 day since now
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 24 * 2, 60 * 1000 * 60 * 24, "humanTime.someDaysAgo")); // 2+ days since now
		// Months
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 24 * 30, 60 * 1000 * 60 * 24 * 30, "humanTime.lastMonth")); // 1 month since now
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 24 * 30 * 2, 60 * 1000 * 60 * 24 * 30, "humanTime.someMonthsAgo")); // 2+ months since now
		// Years
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 24 * 30 * 12, 60 * 1000 * 60 * 24 * 30 * 12, "humanTime.lastYear")); // 1 year since now
		thresholds.add(new HumanTimeThreshold(60 * 1000 * 60 * 24 * 30 * 12 * 2, 60 * 1000 * 60 * 24 * 30 * 12, "humanTime.someYearsAgo"));	// 2+ years since now
		// Ayla's time
		thresholds.add(new HumanTimeThreshold(Long.MAX_VALUE, 1000, "humanTime.bigBang"));	// Bazillion time since now
		
		return withThresholds(thresholds);
	}
	
	/**
	 * Sets the {@link #thresholds}
	 * @param thresholds The {@link HumanTimeThreshold} list
	 * @return {@code this}
	 */
	public HumanTime withThresholds(final List<HumanTimeThreshold> thresholds) {
		this.thresholds = thresholds;
		return this;
	}
	
	/**
	 * Returns a human-readable time for {@code referenceTime} (it shows how long since that time).
	 * 
	 * @param referenceTime The {@code Date} of the reference time
	 * @return A nice, human-readable time :)
	 * @see #humanTime(long)
	 */
	public String humanTime(final Date referenceTime) {
		return humanTime(System.currentTimeMillis() - referenceTime.getTime());
	}
	
	/**
	 * Returns a human-readable time for {@code referenceTime} (it shows how long since that time).
	 * 
	 * @param referenceTime The {@code Calendar} of the reference time
	 * @return A nice, human-readable time :)
	 * @see #humanTime(long)
	 */
	public String humanTime(final Calendar referenceTime) {
		return humanTime(System.currentTimeMillis() - referenceTime.getTimeInMillis());
	}
	
	/**
	 * Returns a human-readable time for {@code timeDiff} milliseconds since now.
	 * 
	 * @param timeDiff The number of milliseconds since the reference time
	 * @return A nice, human-readable time :)
	 * @see #humanTime(Date)
	 * @see #humanTime(Calendar)
	 */
	public String humanTime(final long timeDiff) {
		if (thresholds == null || thresholds.isEmpty()) {
			withDefaultThresholds();
		}
		
		HumanTimeThreshold current, next;
		long timeInUnits = 0;
		for (int i = 0, max = thresholds.size(); i < max; i++) {
			// Is timeDiff within the current threshold bounds?
			current = thresholds.get(i);
			next = thresholds.get(i + 1);
			if (timeDiff >= current.bound && timeDiff < next.bound) {
				if (current.unit > 0) {
					timeInUnits = (long) Math.floor(timeDiff / current.unit);
				}
				else {
					timeInUnits = 0;
				}
				
				return message(current.text, timeInUnits);
			}
		}
		
		return message(resourceBundle == null ? "some time ago" : "humanTime.unknown", 0);
	}
	
	/**
	 * Formats the Human Time message.
	 * 
	 * @param message The message
	 * @param value The value
	 * @return The formatted message
	 */
	private String message(final String message, final long value) {
		String text;
		
		if (resourceBundle != null && resourceBundle.containsKey(message)) {
			text = resourceBundle.getString(message);
		}
		else {
			text = message;
		}
		
		return MessageFormatter.format(text, NumberFormat.getNumberInstance(locale == null ? Locale.getDefault() : locale).format(value)).getMessage();
	}
	
	/**
	 * Represents a Human Time Threshold. It basically tells when one of the texts starts (like after 120000 start using the "x minutes ago")
	 * and how its units should be counted (like, 1 minute = 60000 ms). By default it tries to find an {@code i18n} string and, if it isn't
	 * present, use the text as-is (replacing parameters using {@link MessageFormat}).
	 * 
	 * Everything is {@code public final} because speed.
	 * 
	 * @author Rafael Lins - g0dkar
	 *
	 */
	public static final class HumanTimeThreshold {
		public final long bound;		// Default: 0
		public final long unit;			// Default: 0
		public final String text;		// Default: "{}"
		
		public HumanTimeThreshold(final long bound) {
			this.bound = bound;
			text = "{}";
			unit = 0;
		}
		
		public HumanTimeThreshold(final long bound, final long unit) {
			this.bound = bound;
			this.unit = unit;
			text = "{}";
		}
		
		public HumanTimeThreshold(final long bound, final String text) {
			this.bound = bound;
			this.text = text;
			unit = 0;
		}
		
		public HumanTimeThreshold(final long bound, final long unit, final String text) {
			this.bound = bound;
			this.text = text;
			this.unit = unit;
		}
	}
}