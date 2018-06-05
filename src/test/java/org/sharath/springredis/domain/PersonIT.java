/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.sharath.springredis.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import org.hamcrest.CoreMatchers;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Test;
import org.sharath.spring.test.SpringJpaDataTestConfig;

/**
 *
 * @author Sharath Kulal
 */
public class PersonIT extends SpringJpaDataTestConfig {
   
    private final List<Person> lstPerson = new ArrayList<>();
    
    @After
    public void after() {
        //Remove any objects set outside of global entity manager.
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tran1 = em1.getTransaction();
        tran1.begin();
        for (Person person : lstPerson) {
            Person personDb = em1.find(Person.class, person.getId());
            em1.remove(personDb);
        }
        tran1.commit();
        em1.close();
        
        //clear cache
        emf.getCache().evict(Person.class);
    }
    
    @Test
    public void testDomain() {
        int personId = 1;
        Person person = DomainCreator.createPerson(personId);
        
        em.persist(person);
        em.flush();
        em.clear();
        
        Person fromDb = em.find(Person.class, 1);
        assertThat(fromDb, is(CoreMatchers.notNullValue()));
        assertThat(fromDb.getId(), is(1));
    }
    
    @Test
    public void testPersonWithAddress() {
        Person person = DomainCreator.createPerson(5);
        
        Address address1 = DomainCreator.createAddress(10);
        PersonAddress personAddress1 = new PersonAddress();
        personAddress1.setId(100);
        personAddress1.setPerson(person);
        personAddress1.setAddress(address1);
        
        Address address2 = DomainCreator.createAddress(20);
        PersonAddress personAddress2 = new PersonAddress();
        personAddress2.setId(200);
        personAddress2.setPerson(person);
        personAddress2.setAddress(address2);

        em.persist(personAddress1);
        em.persist(personAddress2);
        em.flush();
        em.clear();
        
        Person fromDb = em.find(Person.class, 5);
        assertThat(fromDb.getId(), is(5));
        Set<Address> setAddress = fromDb.getPersonAddress();
        assertThat(setAddress.size(), is(2));
    }
    
    
    @Test
    public void testCache() {
        Person p1 = new Person();
        p1.setId(1);
        p1.setName("number 1");
        
        EntityManager em1 = emf.createEntityManager();
        EntityTransaction tran1 = em1.getTransaction();
        tran1.begin();
        em1.persist(p1);
        tran1.commit();
        em1.close();
        
        lstPerson.add(p1);

        SessionFactory sessionFactory = emf.unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        
        EntityManager em2 = emf.createEntityManager();
        p1 = em2.find(Person.class, 1);
        em2.close();
        
        assertThat(stats.getSecondLevelCacheHitCount(), is(1L));
    }
    
}
