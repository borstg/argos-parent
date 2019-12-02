package com.rabobank.argos.service.domain.verification;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
public class VerificationRunResult {
    private boolean runIsValid;

    public static VerificationRunResult valid(boolean runIsValid) {
        return VerificationRunResult.builder().runIsValid(runIsValid).build();
    }

    public static VerificationRunResult notOkay() {
        return valid(false);
    }

    public static VerificationRunResult okay() {
        return valid(true);
    }
}