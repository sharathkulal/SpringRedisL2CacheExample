/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sharath.spring.config;

import com.zaxxer.hikari.HikariDataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.SharedCacheMode;
import javax.persistence.CacheRetrieveMode;
import javax.persistence.CacheStoreMode;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author Sharath kulal
 */
public abstract class AbstractSpringJpaDataConfig {
    
    private final Properties dbProperties = new Properties();
    
    @PostConstruct
    public void init() throws IOException {
        InputStream is = getClass().getResourceAsStream("/local.properties");
        dbProperties.load(is);
    }
    
    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws SQLException {
        
        final String jdbcURL = dbProperties.getProperty("jdbc.url");
        final String driverName = dbProperties.getProperty("db.driverName");
        final String dbUser = dbProperties.getProperty("db.user");
        final String dbPassword = dbProperties.getProperty("db.password");
        final Integer initialConnections = Integer.valueOf(dbProperties.getProperty("db.initialConnections"));
        final Integer maxConnections = Integer.valueOf(dbProperties.getProperty("db.maxConnections"));
        final Integer loginTimeout = 5;

        final HikariDataSource dataSource = new HikariDataSource();

        dataSource.setJdbcUrl(jdbcURL);
        dataSource.setDriverClassName(driverName);
        dataSource.setUsername(dbUser);
        dataSource.setPassword(dbPassword);
        dataSource.setMinimumIdle(initialConnections);
        dataSource.setMaximumPoolSize(maxConnections);
        dataSource.setConnectionTestQuery("select 1");
        dataSource.setConnectionTimeout(10000);
        dataSource.setLoginTimeout(loginTimeout);

        //dataSource.setIdleTimeout(10000);
        //dataSource.setAutoCommit(true);

        return dataSource;
    }
    
    @Bean( destroyMethod="close" )
    public EntityManagerFactory entityManagerFactory() throws SQLException, IOException { 
       
      final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

//      final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
//      factory.setJpaVendorAdapter(vendorAdapter);
//            
//      factory.setPackagesToScan(getEntityPackagesToScan());
//      factory.setDataSource(dataSource());
//      factory.afterPropertiesSet();

        final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);

        factory.setPackagesToScan(getEntityPackagesToScan());
        factory.setDataSource(dataSource());
        
        //JPA Configuration
        factory.getJpaPropertyMap().put("javax.persistence.sharedCache.mode", SharedCacheMode.ENABLE_SELECTIVE);
        factory.getJpaPropertyMap().put("javax.persistence.cache.retrieveMode", CacheRetrieveMode.USE);
        factory.getJpaPropertyMap().put("javax.persistence.cache.storeMode", CacheStoreMode.USE);
        
        //factory.getJpaPropertyMap().put("javax.persistence.retrieveMode", CacheRetrieveMode.USE);
        //factory.getJpaPropertyMap().put("javax.persistence.storeMode", CacheStoreMode.USE);
        
        
        //Hibernate Configuration
        String l2CachePrefix = dbProperties.getProperty("l2.cache.prefix");
        factory.getJpaPropertyMap().put("hibernate.cache.use_second_level_cache", "true");//specify true/false
        factory.getJpaPropertyMap().put("hibernate.cache.region_prefix", l2CachePrefix);
        factory.getJpaPropertyMap().put("hibernate.cache.use_query_cache", "false");
        factory.getJpaPropertyMap().put("hibernate.cache.use_structured_entries", "true");
        factory.getJpaPropertyMap().put("hibernate.generate_statistics" ,"true");
        
        //EHCache Configuration - START
        /*factory.getJpaPropertyMap().put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");//works
        factory.getJpaPropertyMap().put("cache.provider_class", "org.hibernate.cache.EhCacheProvider");*/
        
        //factory.getJpaPropertyMap().put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");//works
        //EHCache Configuration - END
        
        //Redis Configuration
        factory.getJpaPropertyMap().put("hibernate.cache.region.factory_class", "org.hibernate.cache.redis.hibernate52.SingletonRedisRegionFactory");
        factory.getJpaPropertyMap().put("redisson-config","classpath:conf/redisson.yml");

        factory.afterPropertiesSet();

      return factory.getObject();
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() throws SQLException, IOException {
      final JpaTransactionManager txManager = new JpaTransactionManager();
      txManager.setEntityManagerFactory(entityManagerFactory());
      return txManager;
    }
    
    public abstract String[] getEntityPackagesToScan();
    
}
