#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

@ignore
Feature: reset

  Background:
    * url karate.properties['server.integration-test-service.baseurl']

  Scenario: create token
    * def minutesEarlier = __arg.minutesEarlier
    * def accountId = __arg.accountId
    * def nowMinMinutes =
"""
function(minMinutes) {
  return Java.type("java.time.OffsetDateTime").now(Java.type("java.time.ZoneOffset").UTC).minusMinutes(minMinutes);
}
"""
    * def datum = call nowMinMinutes minutesEarlier
    * url karate.properties['server.integration-test-service.baseurl']
    Given path '/integration-test/createToken'
    And param accountId = accountId
    And param issuedAt = datum
    When method GET
    Then status 200
