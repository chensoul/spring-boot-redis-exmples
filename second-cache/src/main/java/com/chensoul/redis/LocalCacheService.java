package com.chensoul.redis;

import com.github.benmanes.caffeine.cache.Cache;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.caching.CacheFrontend;
import io.lettuce.core.support.caching.ClientSideCaching;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Local cache Service
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Slf4j
public class LocalCacheService {
	private RedisClient redisClient;
	private Cache cache;

	@Getter
	CacheFrontend cacheFrontend;

	public LocalCacheService(RedisClient redisClient, Cache cache) {
		this.redisClient = redisClient;
		this.cache = cache;
	}

	private StatefulRedisConnection<String, String> connection;

	public void check() {
		if (connection != null && connection.isOpen()) {
			return;
		}

		try {
			connection = redisClient.connect();

			this.cacheFrontend = ClientSideCaching.enable(new CaffeineCacheAccessor(cache), connection, TrackingArgs.Builder.enabled());

			connection.addListener(message -> {
					log.info("push message: {}", message);

					List<Object> content = message.getContent(StringCodec.UTF8::decodeKey);
					if (message.getType().equals("invalidate")) {
						List<String> keys = (List<String>) content.get(1);
						log.info("invalidate keys: {}", keys);
						keys.forEach(key -> cache.invalidate(key));
					}
				}
			);

			log.warn("The redis client connection has been reconnected");

		} catch (
			Exception e) {
			log.error("The redis client connection has been closed");
		}
	}
}
