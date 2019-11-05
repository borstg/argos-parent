package io.jenkins.plugins.argos.recorders;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.rabobank.argos.argos4j.Argos4j;
import com.rabobank.argos.argos4j.Argos4jError;
import com.rabobank.argos.argos4j.Argos4jSettings;
import com.rabobank.argos.argos4j.SigningKey;
import hudson.security.ACL;
import io.jenkins.plugins.argos.ArgosServiceConfiguration;
import jenkins.model.Jenkins;
import lombok.AllArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;

import java.io.IOException;
import java.util.Collections;

@AllArgsConstructor
public class ArgosJenkinsHelper {

    private final String privateKeyCredentialId;
    private final String stepName;
    private final String supplyChainId;


    public Argos4j createArgos() {

        checkProperty(privateKeyCredentialId, "privateKeyCredentialId");
        checkProperty(stepName, "stepName");
        checkProperty(supplyChainId, "supplyChainId");


        String argosServiceBaseUrl = ArgosServiceConfiguration.get().getArgosServiceBaseUrl();
        checkProperty(argosServiceBaseUrl, "argosServiceBaseUrl");
        return new Argos4j(Argos4jSettings.builder()
                .stepName(stepName)
                .argosServerBaseUrl(argosServiceBaseUrl)
                .signingKey(getSigningKey(privateKeyCredentialId))
                .supplyChainId(supplyChainId).build());
    }

    private void checkProperty(String value, String fieldName) {
        if (StringUtils.isBlank(value)) {
            throw new Argos4jError(fieldName + " not configured");
        }
    }

    private SigningKey getSigningKey(String privateKeyCredentialId) {
        try {
            return SigningKey.builder().pemKey(IOUtils.toByteArray(getCredentials(privateKeyCredentialId).getContent())).build();
        } catch (IOException e) {
            throw new Argos4jError(e.getMessage(), e);
        }
    }

    private FileCredentials getCredentials(String privateKeyCredentialId) {
        FileCredentials fileCredential = CredentialsMatchers.firstOrNull(
                CredentialsProvider.lookupCredentials(
                        FileCredentials.class,
                        Jenkins.get(),
                        ACL.SYSTEM,
                        Collections.<DomainRequirement>emptyList()
                ),
                CredentialsMatchers.withId(privateKeyCredentialId)
        );

        if (fileCredential == null)
            throw new RuntimeException(" Could not find credentials entry with ID '" + privateKeyCredentialId + "' ");

        return fileCredential;
    }

}