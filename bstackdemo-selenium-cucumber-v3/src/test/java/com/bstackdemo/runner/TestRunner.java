package com.bstackdemo.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

@CucumberOptions(
        features = "src/test/resources/features/bstackdemo_execution_flow.feature",
        glue = {"com.bstackdemo.steps", "com.bstackdemo.hooks"},
        plugin = {
                "pretty",
                "html:reports/cucumber-report.html",
                "json:reports/cucumber-report.json",
                "junit:reports/cucumber-report.xml"
        },
        monochrome = true,
        publish = false
)
public class TestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
