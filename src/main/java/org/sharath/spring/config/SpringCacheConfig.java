/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sharath.spring.config;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 *
 * @author Sharath Kulal
 */
@Configuration
@EnableCaching
@PropertySource("classpath:/redis.properties")
public class SpringCacheConfig extends CachingConfigurerSupport {

    private static final Logger LOG = LogManager.getLogger();

    @Value("${redis.host-name}")
    private String redisHostName;

    @Value("${redis.port}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
        
        File configFile = new File("src/main/resources/conf/redisson.yml");
        //String absPath = configFile.getAbsolutePath();
        //boolean exists = configFile.exists();
        Config config = Config.fromYAML(configFile);
        
        //config.useClusterServers().addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");
        //String address = "redis://" + redisHostName + ":" + redisPort;
        //config.useSingleServer().setAddress(address);
        return Redisson.create(config);
    }

    /**
     * Simple Config
     * @param redissonClient
     * @return 
     */
    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<String, CacheConfig>();

        // create "testMap" cache with ttl = 24 seconds and maxIdleTime = 12 seconds
        config.put("testMap", new CacheConfig(24 * 60 * 1000, 12 * 60 * 1000));
        return new RedissonSpringCacheManager(redissonClient, config);
    }
    //"redisson-config","classpath:conf/redisson.yml"
    
}
