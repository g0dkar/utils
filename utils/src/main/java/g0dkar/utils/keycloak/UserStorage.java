package g0dkar.utils.keycloak;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.infinispan.Cache;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;

import g0dkar.utils.Configuration;
import g0dkar.utils.StringUtils;
import g0dkar.utils.cache.CacheService;

/**
 * Handles most of the "another User" operations. Mostly getting info about
 * users from their ID (which in turn comes from Keycloak)
 * 
 * @author Rafael Lins
 *
 */
@Named("userStorage")
@RequestScoped
public class UserStorage implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Pattern UUID_PATTERN = Pattern.compile(".+([a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12})");
	
	private final Logger log;
	private final CacheService cs;
	private final KeycloakService keycloak;
	
	/** @deprecated CDI */ @Deprecated
	UserStorage() { this(null, null, null, null); }
	
	@Inject
	public UserStorage(final Configuration conf, final CacheService cs, final KeycloakService keycloak, final Logger log) {
		this.log = log;
		this.cs = cs;
		this.keycloak = keycloak;
		
		if (conf != null) {
			keycloak.init(conf);
		}
	}
	
	public UserRepresentation get(final String id) {
		final Cache<String, UserRepresentation> cache = cs.getCache("users");
		UserRepresentation user = cache.get(id);
		
		if (user != null) {
			return user;
		} else {
			try {
				final UserResource userResource = keycloak.realm().users().get(id);
				
				if (userResource != null) {
					user = userResource.toRepresentation();
					cache.put(user.getId(), user);
					cache.put(user.getEmail(), user);
					
					if (user.getAttributes() == null) {
						user.setAttributes(new HashMap<>());
					}
					
					if (!user.getAttributes().containsKey("avatar")) {
						user.getAttributes().put("avatar", Arrays.asList("https://gravatar.com/avatar/" + StringUtils.md5(user.getEmail())));
					}
				}
			} catch (final Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Exception while looking up Keycloak User", e);
				}
			}
		}
		
		return user;
	}
	
	public boolean exists(final String id) {
		final Cache<String, UserRepresentation> cache = cs.getCache("users");
		return cache.containsKey(id) || get(id) != null;
	}
	
	public UserRepresentation fromEmail(final String email) {
		final Cache<String, UserRepresentation> cache = cs.getCache("users");
		UserRepresentation user = cache.get(email.trim().toLowerCase());
		
		if (user != null) {
			return user;
		} else {
			try {
				final List<UserRepresentation> found = keycloak.realm().users().search(null, null, null, email, 0, 1);
				
				if (found != null && !found.isEmpty()) {
					for (final UserRepresentation userRepresentation : found) {
						if (userRepresentation.getEmail().equalsIgnoreCase(email.trim())) {
							user = userRepresentation;
							cache.put(user.getId(), user);
							cache.put(user.getEmail().trim().toLowerCase(), user);
							
							if (user.getAttributes() == null) {
								user.setAttributes(new HashMap<>());
							}
							
							if (!user.getAttributes().containsKey("avatar")) {
								user.getAttributes().put("avatar", Arrays.asList("https://gravatar.com/avatar/" + StringUtils.md5(user.getEmail())));
							}
						}
					}
				}
			} catch (final Exception e) {
				if (log.isErrorEnabled()) {
					log.error("Exception while looking up Keycloak User", e);
				}
			}
		}
		
		return user;
	}
	
	public String create(final String email) {
		return create(email, null, null, null, null);
	}
	
	public String create(final String email, final String firstName, final String lastName) {
		return create(email, null, null, firstName, lastName);
	}
	
	public String create(final String email, final String username, final String password, final String firstName, final String lastName) {
		final CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password == null ? randomPassword() : password);
		
		final UserRepresentation newUser = new UserRepresentation();
		newUser.setUsername(username == null ? email : username);
		newUser.setEmail(email);
		newUser.setEmailVerified(false);
		newUser.setEnabled(true);
		newUser.setFirstName(firstName);
		newUser.setLastName(lastName);
		newUser.setCredentials(Arrays.asList(credential));
		newUser.setAttributes(new HashMap<>());
		newUser.getAttributes().put("avatar", Arrays.asList("https://gravatar.com/avatar/" + StringUtils.md5(email)));
		
		try {
			final Response res = keycloak.realm().users().create(newUser);
			
			if (res.getStatus() == Response.Status.CREATED.getStatusCode()) {
				final Matcher matcher = UUID_PATTERN.matcher(res.getHeaderString("Location"));
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		catch (final Exception e) {
			log.error("Error while creating new user", e);
		}
		
		return null;
	}
	
	/**
	 * Returns a more simplistic user, with only the bare minimal info about it
	 * in a {@link Map}
	 * 
	 * @param id
	 *            The User ID
	 * @return {@code id}, {@code type} (always equal to {@code "profile"}),
	 *         {@code first_name}, {@code last_name}, {@code name} (first name +
	 *         last, if available, or the username) and {@code avatar}
	 */
	public Map<String, Object> getSimple(final String id) {
		return getSimple(get(id));
	}
	
	/**
	 * Returns a more simplistic user, with only the bare minimal info about it
	 * in a {@link Map}
	 * 
	 * @param user
	 *            The User
	 * @return {@code id}, {@code type} (always equal to {@code "profile"}),
	 *         {@code first_name}, {@code last_name}, {@code name} (first name +
	 *         last, if available, or the username) and {@code avatar}
	 */
	public Map<String, Object> getSimple(final UserRepresentation user) {
		if (user != null) {
			final Map<String, Object> userMap = new HashMap<>(6);
			userMap.put("id", user.getId());
			userMap.put("type", "profile");
			userMap.put("first_name", user.getFirstName());
			userMap.put("last_name", user.getLastName());
			userMap.put("name", user.getFirstName() == null ? user.getUsername() : user.getLastName() != null ? user.getFirstName() + " " + user.getLastName() : user.getFirstName());
			userMap.put("avatar", ((List<String>) user.getAttributes().get("avatar")).get(0));
			return userMap;
		} else {
			return null;
		}
	}
	
	// Order of the keys on my keyboard... If I'll access them randomly, why have them ordered?
	// Numbers are here to make sure they have a chance to show up
	private static final String LETTERS = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
	private static final String SPECIAL = "0123456789:.^%=-+[]{}~`_ ,<>å¡!²³&ä®";
	/** @return A random 100 characters long string */
	private String randomPassword() {
		return randomPassword(100, true);
	}
	
	public static String randomPassword(final int size, final boolean special) {
		final SecureRandom r = new SecureRandom();
		final StringBuilder pwd = new StringBuilder();
		
		for (int i = 0; i < size; i++) {
			if (special) {
				if (r.nextBoolean()) {
					pwd.append(LETTERS.charAt(r.nextInt(LETTERS.length())));
				}
				else {
					pwd.append(SPECIAL.charAt(r.nextInt(SPECIAL.length())));
				}
			}
			else {
				pwd.append(LETTERS.charAt(r.nextInt(LETTERS.length())));
			}
		}
		
		return pwd.toString();
	}
}
