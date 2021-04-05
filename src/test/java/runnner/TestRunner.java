package runnner;

import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;


@RunWith(Cucumber.class)
@io.cucumber.junit.CucumberOptions(
		features = "/src/test/resources",
		glue = {"stepDefinition"}
		)

public class TestRunner {

}
