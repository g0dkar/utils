package g0dkar.utils.cache;

import java.util.Set;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;

import org.infinispan.Cache;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.CacheContainerStats;

/**
 * Provides an easier, injectable access to {@link Cache} instances
 * @author Rafael Lins
 *
 */
@ApplicationScoped
public class CacheService {
	@Resource(lookup = "java:/cache/respondeai")
	private EmbeddedCacheManager cacheManager;
	
	public EmbeddedCacheManager getManager() {
		return cacheManager;
	}

	public <K, V> Cache<K, V> getCache() {
		return cacheManager.getCache();
	}

	public <K, V> Cache<K, V> getCache(final String cacheName) {
		return cacheManager.getCache(cacheName);
	}

	public ComponentStatus getStatus() {
		return cacheManager.getStatus();
	}

	public Set<String> getCacheNames() {
		return cacheManager.getCacheNames();
	}

	public <K, V> Cache<K, V> getCache(final String cacheName, final boolean createIfAbsent) {
		return cacheManager.getCache(cacheName, createIfAbsent);
	}

	public <K, V> Cache<K, V> getCache(final String cacheName, final String configurationName) {
		return cacheManager.getCache(cacheName, configurationName);
	}

	public <K, V> Cache<K, V> getCache(final String cacheName, final String configurationTemplate, final boolean createIfAbsent) {
		return cacheManager.getCache(cacheName, configurationTemplate, createIfAbsent);
	}

	public CacheContainerStats getStats() {
		return cacheManager.getStats();
	}
}
