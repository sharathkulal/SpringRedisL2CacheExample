/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sharath.springredis.service;

import java.util.Collection;
import java.util.UUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.After;
import org.junit.Test;
import org.sharath.spring.test.SpringRedisTestConfig;
import org.springframework.cache.Cache;

/**
 *
 * @author Sharath Kulal
 */
public class SpringRedisTest extends SpringRedisTestConfig{
    
    private final String prefix = "unittest:";//Redis namespace (displays this as folder)
    private final String key = prefix+UUID.randomUUID().toString();
    
    @After
    public void afterTest() {
        
    }
    
    @Test
    public void testSet() {
        String value = "value";
        redissonClient.getBucket(key).set(value);
        String cacheValue = (String)redissonClient.getBucket(key).get();
        assertThat(cacheValue, is(value));
    }
    
    @Test
    public void testSpringRedisCacheManager() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        assertThat(cacheNames.size(), is(1));// defined in SpringCacheConfig config.put("testMap"...
        assertThat(cacheNames.contains("testMap"), is(true));
        
    }
 
    @Test
    public void testSpringRedisCacheManager_put() {
        String value = "value";
        cacheManager.getCache("testMap").put(key, value);
        Cache cache = cacheManager.getCache("testMap");
        String cacheValue = (String)cache.get(key).get();
        assertThat(cacheValue, is(value));
    }
}
