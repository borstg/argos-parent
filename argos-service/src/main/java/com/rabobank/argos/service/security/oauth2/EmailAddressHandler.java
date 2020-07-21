package com.rabobank.argos.service.security.oauth2;

public interface EmailAddressHandler {
    String getEmailAddress(String token, String emailUri);
}
