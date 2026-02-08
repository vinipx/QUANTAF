package io.github.vinipx.quantaf.bdd.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Cucumber TestNG runner that bridges Cucumber BDD features to TestNG execution.
 */
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "io.github.vinipx.quantaf.bdd.steps",
        plugin = {
                "pretty",
                "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
                "html:build/reports/cucumber/cucumber.html",
                "json:build/reports/cucumber/cucumber.json"
        },
        tags = "@smoke or @regression"
)
public class CucumberTestRunner extends AbstractTestNGCucumberTests {

    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
