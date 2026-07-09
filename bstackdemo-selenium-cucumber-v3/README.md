# BStackDemo End-to-End Automation Framework

A robust UI Test Automation Framework developed using **Java, Selenium WebDriver, Cucumber BDD, TestNG, Maven, Extent Reports, and Allure Reports** for automating end-to-end user journeys on the BrowserStack Demo application.

The framework follows industry-standard automation practices including **Page Object Model (POM)**, reusable utilities, centralized driver management, cross-browser testing, advanced reporting, screenshot capture, and Behavior-Driven Development (BDD).

---

## Project Overview

This project automates critical e-commerce functionalities of the BrowserStack Demo application, including:

- User Authentication
- Product Validation
- Add to Cart
- Cart Management
- Checkout Process
- Shipping Details Validation
- Order Placement
- Email Validation
- Address & Pincode Verification
- Cross-Browser Testing

The framework is designed to provide:

- Reusable and maintainable test scripts
- Scalable framework architecture
- Faster execution
- Better test coverage
- Detailed reporting and debugging support

---

## Technology Stack

- Java
- Selenium WebDriver
- Cucumber BDD
- TestNG
- Maven
- WebDriverManager
- Extent Reports
- Allure Reports
- Apache Commons
- BrowserStack Demo Application

---

## Framework Design Pattern

This framework follows the **Page Object Model (POM)** design pattern to separate page actions from test logic.

### Page Classes

| Page Class | Responsibility |
|------------|---------------|
| HomePage | Product listing, navigation, and product validations |
| LoginPage | User authentication and login operations |
| CartPage | Cart management and validations |
| CheckoutPage | Shipping details and order placement |

### Benefits

- Improved code maintainability
- Better reusability
- Reduced code duplication
- Enhanced readability
- Easy scalability

---

## Project Structure

```text
bstackdemo-selenium-cucumber-v3

├── pom.xml
├── testng.xml
├── README.md
│
├── src
│   └── test
│       ├── java
│       │   └── com.bstackdemo
│       │
│       │       ├── base
│       │       │   └── DriverFactory.java
│       │       │
│       │       ├── hooks
│       │       │   └── Hooks.java
│       │       │
│       │       ├── pages
│       │       │   ├── HomePage.java
│       │       │   ├── LoginPage.java
│       │       │   ├── CartPage.java
│       │       │   └── CheckoutPage.java
│       │       │
│       │       ├── runner
│       │       │   └── TestRunner.java
│       │       │
│       │       ├── steps
│       │       │   └── BStackDemoSteps.java
│       │       │
│       │       └── utils
│       │           ├── ConfigReader.java
│       │           ├── ElementUtil.java
│       │           ├── ScenarioContext.java
│       │           ├── ScreenshotUtil.java
│       │           └── ValidationUtil.java
│       │
│       └── resources
│           ├── config
│           │   └── config.properties
│           │
│           └── features
│               └── bstackdemo_execution_flow.feature
│
├── reports
├── screenshots
├── allure-results
└── allure-report
```

---

## Framework Components

### DriverFactory

Responsible for:

- Browser initialization
- Browser configuration
- Driver lifecycle management
- Cross-browser support

### Hooks

Responsible for:

- Browser setup before execution
- Browser closure after execution
- Screenshot capture on failures
- Scenario lifecycle handling

### Page Classes

Contains web element locators and reusable page actions.

### Step Definitions

Contains implementation logic for all Cucumber scenarios.

### Utility Classes

Provides reusable methods for:

- Configuration management
- Selenium actions
- Scenario data sharing
- Screenshot handling
- Validation logic

---

## Scenario Organization

Instead of creating separate Cucumber scenarios for every individual test case, multiple related test cases are grouped into execution-flow scenarios.

### Current Coverage

- **64 Test Cases**
- **12 Execution Flow Scenarios**

### Benefits

- Reduced browser launches
- Faster execution
- Better maintainability
- Cleaner feature files
- Improved resource utilization

---

## Browser Execution

Browser execution is controlled using Cucumber tags.

### Supported Browsers

```text
@chrome
@edge
@firefox
```

### Example

```gherkin
@chrome
Scenario: User Login Validation
```

The framework dynamically launches the corresponding browser before scenario execution.

---

## Test Execution

### Execute Complete Test Suite

```bash
mvn clean test
```

### Execute Specific Scenario Group

```bash
mvn clean test -Dcucumber.filter.tags="@TC032_TO_TC038"
```

### Execute Checkout Scenarios

```bash
mvn clean test -Dcucumber.filter.tags="@checkout"
```

### Execute In Headless Mode

```bash
mvn clean test -Dheadless=true
```

---

## Validation Coverage

The automation suite validates:

- Login Functionality
- Product Details
- Product Vendor Information
- Product Pricing
- Add to Cart Flow
- Cart Total Calculation
- Shipping Information
- Email Validation
- Address Verification
- Pincode Validation
- Checkout Process
- Order Confirmation

---

## Reporting

The framework generates multiple reports after execution for detailed analysis and debugging.

### Extent Reports

Extent Reports provide:

- Interactive Dashboard
- Execution Summary
- Pass/Fail Statistics
- Step-Level Details
- Screenshot Integration
- Easy Failure Analysis

### Cucumber Reports

Generated under:

```text
reports/
├── cucumber-report.html
├── cucumber-report.json
└── cucumber-report.xml
```

### Allure Reports

Allure Reports provide:

- Rich Visual Dashboard
- Execution History
- Detailed Step Information
- Failure Analysis
- Screenshot Attachments
- Trend Analysis

### Generate Allure Report

```bash
allure serve allure-results
```

### Generate Static Allure Report

```bash
allure generate allure-results --clean -o allure-report
```

Generated folders:

```text
allure-results/
allure-report/
```

---

## Screenshot Capture

Screenshots are automatically captured for failed scenarios.

```text
screenshots/
```

These screenshots help in faster defect analysis and debugging.

---

## Key Features

✅ Page Object Model (POM)

✅ Selenium WebDriver Automation

✅ Cucumber BDD Framework

✅ TestNG Integration

✅ Maven Build Management

✅ Cross-Browser Testing

✅ Dynamic Browser Selection

✅ Scenario-Based Execution Flow

✅ Reusable Utility Classes

✅ Centralized Driver Management

✅ Configuration-Driven Framework

✅ Screenshot Capture on Failures

✅ Extent Report Integration

✅ Allure Report Integration

✅ End-to-End E-Commerce Workflow Validation

✅ Maintainable and Scalable Automation Architecture

---

## Future Enhancements

- Parallel Test Execution
- Jenkins CI/CD Integration
- Docker-Based Execution
- BrowserStack Cloud Execution
- Data-Driven Testing using Excel
- Database Validation
- GitHub Actions Integration

---

## Author

**Nitheesh Chennoju**  
**Project Engineer | SDET Trainee**

This Capstone Project demonstrates the implementation of a modern automation framework using **Java, Selenium WebDriver, Cucumber BDD, TestNG, Maven, Page Object Model (POM), Extent Reports, and Allure Reports** for end-to-end testing of a real-world e-commerce application.
