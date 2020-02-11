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

Feature: SupplyChain

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')

  Scenario: store supplychain with valid name should return a 201
    Given path '/api/supplychain'
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def locationHeader = result.responseHeaders['Location'][0]
    * match result.response == { name: 'name', id: '#uuid', parentLabelId: '#uuid' }
    * match locationHeader contains 'api/supplychain/'

  Scenario: store supplychain with non unique name should return a 400
    * def supplyChainResponse = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    Given path '/api/supplychain'
    And request  {"name":"name", parentLabelId: "#(supplyChainResponse.response.parentLabelId)"}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
    And match response.message contains 'supply chain with name: name and parentLabelId'

  Scenario: update supplychain should return a 200
    * def supplyChainResponse = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def labelResult = call read('classpath:feature/label/create-label.feature') {name: otherlabel}
    Given path '/api/supplychain/'+supplyChainResponse.response.id
    And request  {"name":"supply-chain-name", parentLabelId: "#(labelResult.response.id)"}
    And header Content-Type = 'application/json'
    When method PUT
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#(supplyChainResponse.response.id)', parentLabelId: '#(labelResult.response.id)' }

  Scenario: get supplychain with valid id should return a 200
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'name'}
    * def restPath = '/api/supplychain/'+result.response.id
    Given path restPath
    When method GET
    Then status 200
    And match response == { name: 'name', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: get supplychain with invalid id should return a 404
    Given path '/api/supplychain/invalidid'
    When method GET
    Then status 404
    And match response == {"message":"supply chain not found : invalidid"}

  Scenario: query supplychain with name should return a 200
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'supply-chain-name'}
    Given path '/api/supplychain'
    And param supplyChainName = 'supply-chain-name'
    And param pathToRoot = 'label'
    When method GET
    Then status 200
    And match response == { name: 'supply-chain-name', id: '#uuid', parentLabelId: '#uuid' }

  Scenario: query supplychain with name and non existing label should return a 404
    * def result = call read('create-supplychain-with-label.feature') { supplyChainName: 'supply-chain-name'}
    Given path '/api/supplychain'
    And param supplyChainName = 'supply-chain-name'
    And param pathToRoot = 'otherlabel'
    When method GET
    Then status 404