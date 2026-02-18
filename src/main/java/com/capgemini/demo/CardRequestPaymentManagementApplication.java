package com.capgemini.demo;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title="User API",
                version="1.0",
                description = "API documentation for authenticating users"
        )
)

@SpringBootApplication
public class CardRequestPaymentManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(CardRequestPaymentManagementApplication.class, args);
	}

}
