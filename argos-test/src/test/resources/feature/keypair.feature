Feature: Keypair
  Background:
    * url karate.properties['server.baseurl']
  Scenario: store key with invalid key should return a 400 error
    Given path '/api/key'
    And request {"keyId": "testkeyid","publicKey": "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC/Ldm84IhBvssdweZOZSPcx87J0Xy63g0JhlOYlr66aKmbXz5YD+J+b4NlIIbvaa5sEg4FS0+gkOPgexqCzgRUqHK5coLchpuLFggmDiL4ShqGIvqb/HPq7Aauk8Ss+0TaHfkJjd2kEBPRgWLII1gytjKkqlRGD/LxRtsppnleQwIDAQAB","encryptedPrivateKey": null}
    And header Content-Type = 'application/json'
    When method POST
    Then status 400
