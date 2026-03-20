import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.capgemini.demo"}
)
/*
@Configuration(
        value = "classpath:CucumberSpringConfig.java"
)

 */
public class TestCreateCaseFeatureRunner {
}
