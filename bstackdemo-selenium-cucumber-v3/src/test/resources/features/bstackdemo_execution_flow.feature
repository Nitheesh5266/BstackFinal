Feature: BStackDemo scenarios sorted by real user execution flow
  Feature file contains scenario groups only. Individual test cases TC001 to TC064 are implemented inside step definitions in the same scenario order.
  Browser invocation happens once per Scenario through Hooks.java. It does not open a new browser for each individual test case.

  @chrome @registration @email @execution_flow @TC001_TO_TC005
  Scenario: User registration availability and email validation scenarios
    Given I am on the BStackDemo home page
    When I execute registration and email validation test cases TC001 to TC005
    Then all executed test cases should pass

  @chrome @login @execution_flow @TC006_TO_TC012
  Scenario: User login authentication and logout scenarios
    Given I am on the BStackDemo home page
    When I execute login authentication and logout test cases TC006 to TC012
    Then all executed test cases should pass

  @chrome @home @product_listing @execution_flow @TC013_TO_TC017
  Scenario: Home page and product listing scenarios
    Given I am on the BStackDemo home page
    When I execute home page and product listing test cases TC013 to TC017
    Then all executed test cases should pass

  @chrome @filter @execution_flow @TC018_TO_TC021
  Scenario: Product vendor filter scenarios
    Given I am on the BStackDemo home page
    When I execute product vendor filter test cases TC018 to TC021
    Then all executed test cases should pass

  @chrome @sort @execution_flow @TC022_TO_TC024
  Scenario: Product price sort scenarios
    Given I am on the BStackDemo home page
    When I execute product price sort test cases TC022 to TC024
    Then all executed test cases should pass

  @chrome @cart @execution_flow @TC025_TO_TC031
  Scenario: Cart add and cart view scenarios
    Given I am on the BStackDemo home page
    When I execute cart add and cart view test cases TC025 to TC031
    Then all executed test cases should pass

  @chrome @cart @quantity @remove @execution_flow @TC032_TO_TC038
  Scenario: Cart quantity total and remove scenarios
    Given I am on the BStackDemo home page
    When I execute cart quantity total and remove test cases TC032 to TC038
    Then all executed test cases should pass

  @chrome @checkout @address @pincode @validation @execution_flow @TC039_TO_TC055
  Scenario: Checkout address pincode validation and order scenarios
    Given I am on the BStackDemo home page
    When I execute checkout address pincode validation and order test cases TC039 to TC055
    Then all executed test cases should pass

  @chrome @logout @security @execution_flow @TC056_TO_TC057
  Scenario: Logout and browser security scenarios
    Given I am on the BStackDemo home page
    When I execute logout and browser security test cases TC056 to TC057
    Then all executed test cases should pass

  @chrome @framework @execution_flow @TC058_TO_TC061
  Scenario: Framework wait screenshot and report scenarios
    Given I am on the BStackDemo home page
    When I execute framework wait screenshot and report test cases TC058 to TC061
    Then all executed test cases should pass

  @chrome @compatibility @execution_flow @TC062_TC064
  Scenario: Chrome browser compatibility and responsive scenarios
    Given I am on the BStackDemo home page
    When I execute chrome compatibility and responsive test cases TC062 and TC064
    Then all executed test cases should pass

  @edge @compatibility @execution_flow @TC063
  Scenario: Edge browser compatibility scenario
    Given I am on the BStackDemo home page
    When I execute edge compatibility test case TC063
    Then all executed test cases should pass
