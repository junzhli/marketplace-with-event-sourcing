/* (C)2022 */
package com.jeremyli.account.database;

import javax.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;

@EnableJpaRepositories(basePackages = {"com.jeremyli.account.domain", "com.jeremyli.common.outbox"})
@Configuration
public class DBConfig {
//    @Bean("transactionManager")
//    public JpaTransactionManager jpaTransactionManager(EntityManagerFactory entityManagerFactory) {
//        return new JpaTransactionManager(entityManagerFactory);
//    }
}
