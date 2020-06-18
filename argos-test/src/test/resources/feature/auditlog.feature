@ignore
Feature: auditlog

  Background:
    * url karate.properties['server.integration-test-service.baseurl']
    * configure headers = { 'Content-Type': 'text/plain;charset=UTF-8' }

  Scenario: get auditlog should return 200
    Given path '/integration-test/audit-log'
    When method GET
    Then status 200