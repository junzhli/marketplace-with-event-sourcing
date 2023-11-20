package com.jeremyli.orderview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.web.reactive.config.EnableWebFlux;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class, // same as hibernate
		DataSourceTransactionManagerAutoConfiguration.class, // same as hibernate
		HibernateJpaAutoConfiguration.class, // disable auto-configuration for hibernate orm framework
		WebMvcAutoConfiguration.class, // disable webmvc, use webflux instead
})
public class OrderViewApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderViewApplication.class, args);
	}

}
