package com.walmartlabs.concord.plugins.secrets;

public class SecretsClientTest {

    public static void main(String[] args) throws Exception {
        SecretsClient client = new SecretsClient("http://localhost:8080");
        client.addPlainSecret("jvanzyl", "hello", false, null, "hello".getBytes());
    }
}
