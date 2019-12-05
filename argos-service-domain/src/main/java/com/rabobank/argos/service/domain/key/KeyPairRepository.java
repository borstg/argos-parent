package com.rabobank.argos.service.domain.key;

import com.rabobank.argos.domain.key.KeyPair;

import java.util.Optional;

public interface KeyPairRepository {
    void save(KeyPair keyPair);

    Optional<KeyPair> findByKeyId(String keyId);

    boolean exists(String keyId);
}
