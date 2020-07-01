Feature: Search Account

  Background:
    * url karate.properties['server.baseurl']
    * call read('classpath:feature/reset.feature')
    * def defaultTestData = call read('classpath:default-test-data.js')
    * def keyId = defaultTestData.serviceAccount['default-sa2'].keyId
    * configure headers = call read('classpath:headers.js') { token: #(defaultTestData.adminToken)}
    * def supplyChain = call read('classpath:feature/supplychain/create-supplychain.feature') { supplyChainName: 'name', parentLabelId: #(defaultTestData.defaultRootLabel.id)}

  Scenario: search account by key id should return a 200
    Given path '/api/supplychain/'+supplyChain.id+'/account/key'
    And param keyIds = keyId
    When method GET
    Then status 200
    * def expectedResponse = read('classpath:testmessages/account/search-account-keyinfo-response.json')
    And match response contains expectedResponse