package com.capgemini.demo;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@AutoConfigureMockMvc(addFilters = false)
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true",
                "spring.jpa.properties.hibernate.format_sql=true",

                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
public class CucumberSpringConfig {
}
