package com.chensoul.redis;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.lettuce.core.RedisClient;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 * Redis Configuration
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since 1.0.0
 */
@Configuration
public class RedisConfiguration {

	@Bean
	public CommandLineRunner init(LocalCacheService localCacheService) {
		return args -> {
			while (true) {
				localCacheService.check();
				Thread.sleep(1000);
			}
		};
	}

	@Bean
	public Cache<String, Object> localCahce() {
		return Caffeine.newBuilder()
			.initialCapacity(100)
			.maximumSize(1000)
			.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();
	}

	@Bean
	public RedisClient redisClient(@Autowired LettuceConnectionFactory connectionFactory) {
		return (RedisClient) connectionFactory.getNativeClient();
	}

	@Bean
	public LocalCacheService localCacheService(RedisClient redisClient, Cache cache) {
		return new LocalCacheService(redisClient, cache);

	}
}
