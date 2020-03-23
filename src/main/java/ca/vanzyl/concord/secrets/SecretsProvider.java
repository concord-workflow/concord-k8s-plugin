package com.walmartlabs.concord.secrets;

public interface SecretsProvider {

    void put(String secretName, String secretText);
    String get(String secretName);
    void remove(String secretName);
}