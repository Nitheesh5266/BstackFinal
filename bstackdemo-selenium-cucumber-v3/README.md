# BStackDemo Selenium Cucumber Maven Project - v3

This project uses Java + Selenium WebDriver + Cucumber + TestNG.

## What changed in v3

- Browser now opens **once per scenario group**, not once per individual test case.
- Feature file contains **12 execution-flow scenarios** instead of 64 individual TC scenarios.
- All individual test cases **TC001 to TC064** are implemented inside `BStackDemoSteps.java` and sorted under the matching scenario sections.
- `TestRunner.java` forces Cucumber/TestNG to run scenarios sequentially using `@DataProvider(parallel = false)`.
- Fixed the `NumberFormatException: multiple points` issue by replacing amount parsing with regex-based numeric extraction.
- Fixed email validation so `automation.user@gmail` fails based on stricter business email validation requiring a domain extension.
- Fixed Apple vendor validation by mapping Apple products to iPhone/iPad/Mac names instead of expecting the word `Apple` in product titles.
- Improved login dropdown selection and logged-in assertion.
- Address and pincode validation still uses browser-based real-time lookup through Selenium.

## Project structure

```text
bstackdemo-selenium-cucumber-v3
├── pom.xml
├── testng.xml
├── README.md
└── src/test
    ├── java/com/bstackdemo
    │   ├── base/DriverFactory.java
    │   ├── hooks/Hooks.java
    │   ├── runner/TestRunner.java
    │   ├── steps/BStackDemoSteps.java
    │   └── utils
    │       ├── ConfigReader.java
    │       ├── ElementUtil.java
    │       ├── ScenarioContext.java
    │       ├── ScreenshotUtil.java
    │       └── ValidationUtil.java
    └── resources
        ├── config/config.properties
        └── features/bstackdemo_execution_flow.feature
```

## How browser invocation works

`Hooks.java` reads the scenario tag before every scenario:

- `@chrome` opens Chrome
- `@edge` opens Edge
- `@firefox` opens Firefox

Because the feature file has 12 scenario groups, a full run should invoke the browser 12 times, not 64 times.

## Run all scenarios

```bash
mvn clean test
```

## Run only one scenario group

```bash
mvn clean test -Dcucumber.filter.tags="@TC032_TO_TC038"
```

## Run only checkout scenarios

```bash
mvn clean test -Dcucumber.filter.tags="@checkout"
```

## Run in headless mode

```bash
mvn clean test -Dheadless=true
```

## Reports

After execution, reports are generated under:

```text
reports/cucumber-report.html
reports/cucumber-report.json
reports/cucumber-report.xml
```

Screenshots are saved under:

```text
screenshots/
```

## Important note

Cucumber treats each `Scenario` as an executable test. In v2, 64 test cases were written as 64 scenarios, so the browser opened 64 times. In v3, individual test cases are grouped inside scenario-based step definitions, so the browser opens once for each scenario group.
