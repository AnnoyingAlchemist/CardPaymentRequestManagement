package com.capgemini.demo;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class CardRequestPaymentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardRequestPaymentManagementApplication.class, args);
	}

}
