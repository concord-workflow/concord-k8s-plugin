package com.walmartlabs.concord.plugins.secrets;

import com.squareup.okhttp.OkHttpClient;
import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.ApiException;
import com.walmartlabs.concord.ApiResponse;
import com.walmartlabs.concord.client.ClientUtils;
import com.walmartlabs.concord.client.ConcordApiClient;
import com.walmartlabs.concord.client.SecretEntry;
import com.walmartlabs.concord.client.SecretOperationResponse;
import com.walmartlabs.concord.client.StartProcessResponse;
import com.walmartlabs.concord.common.secret.BinaryDataSecret;
import com.walmartlabs.concord.common.secret.KeyPair;
import com.walmartlabs.concord.common.secret.UsernamePassword;
import com.walmartlabs.concord.sdk.Secret;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConcordSecretsClient {

    public static final String SECRET_TYPE = "X-Concord-SecretType";
    private static final int RETRY_COUNT = 3;
    private static final long RETRY_INTERVAL = 5000;
    private final ApiClient client;

    public ConcordSecretsClient(String url, String apiKey) {
        this.client = createClient(url, apiKey);
    }

    public ConcordSecretsClient(ApiClient apiClient) {
        this.client = apiClient;
    }

    public StartProcessResponse start(Map<String, Object> input) throws ApiException {
        return request("/api/v1/process", input, StartProcessResponse.class);
    }

    public <T> T request(String uri, Map<String, Object> input, Class<T> entityType) throws ApiException {
        ApiResponse<T> resp = ClientUtils.postData(client, uri, input, entityType);

        int code = resp.getStatusCode();
        if (code < 200 || code >= 300) {
            if (code == 403) {
                throw new ApiException("Forbidden! " + resp.getData());
            }

            throw new ApiException("Request error: " + code);
        }

        return resp.getData();
    }

    public SecretOperationResponse postSecret(String orgName, Map<String, Object> input) throws ApiException {
        return request("/api/v1/org/" + orgName + "/secret", input, SecretOperationResponse.class);
    }

    public SecretOperationResponse generateKeyPair(String orgName, String projectName, String name, boolean generatePassword, String storePassword) throws ApiException {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("generatePassword", generatePassword);
        m.put("type", SecretEntry.TypeEnum.KEY_PAIR.toString());
        if (storePassword != null) {
            m.put("storePassword", storePassword);
        }

        if (projectName != null) {
            m.put("project", projectName);
        }

        return postSecret(orgName, m);
    }

    public SecretOperationResponse addPlainSecret(String orgName, String name, boolean generatePassword, String storePassword, byte[] secret) throws ApiException {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("type", SecretEntry.TypeEnum.DATA.toString());
        m.put("generatePassword", generatePassword);
        m.put("data", secret);
        if (storePassword != null) {
            m.put("storePassword", storePassword);
        }

        return postSecret(orgName, m);
    }

    public SecretOperationResponse addUsernamePassword(String orgName, String projectName, String name, boolean generatePassword, String storePassword, String username, String password) throws ApiException {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("type", SecretEntry.TypeEnum.USERNAME_PASSWORD.toString());
        m.put("generatePassword", generatePassword);
        m.put("username", username);
        m.put("password", password);
        if (projectName != null) {
            m.put("project", projectName);
        }
        if (storePassword != null) {
            m.put("storePassword", storePassword);
        }

        return postSecret(orgName, m);
    }

    public BinaryDataSecret secret(String orgName, String secretName) throws Exception {
        return secret(orgName, secretName, null, SecretEntry.TypeEnum.DATA);
    }

    public BinaryDataSecret keypair(String orgName, String secretName) throws Exception {
        return secret(orgName, secretName, null, SecretEntry.TypeEnum.KEY_PAIR);
    }

    public BinaryDataSecret usernamePassword(String orgName, String secretName) throws Exception {
        return secret(orgName, secretName, null, SecretEntry.TypeEnum.USERNAME_PASSWORD);
    }

    private <T extends Secret> T secret(String orgName, String secretName, String password, SecretEntry.TypeEnum type) throws Exception {
        String path = "/api/v1/org/" + orgName + "/secret/" + secretName + "/data";

        ApiResponse<File> r = null;

        Map<String, Object> params = new HashMap<>();
        String pwd = password;
        if (password == null) {
            pwd = ""; // NOSONAR
        }
        params.put("storePassword", pwd);

        try {
            r = ClientUtils.withRetry(RETRY_COUNT, RETRY_INTERVAL,
                    () -> ClientUtils.postData(client, path, params, File.class));

            if (r.getData() == null) {
                throw new IllegalArgumentException("Secret not found");
            }

            SecretEntry.TypeEnum actualSecretType = SecretEntry.TypeEnum.valueOf(ClientUtils.getHeader(SECRET_TYPE, r));

            if (type != null && type != actualSecretType) {
                throw new IllegalArgumentException("Expected " + type + " got " + actualSecretType);
            }

            return readSecret(actualSecretType, Files.readAllBytes(r.getData().toPath()));
        } catch (ApiException e) {
            if (e.getCode() == 404) {
                throw new IllegalArgumentException("Secret not found");
            }
            throw e;
        } finally {
            if (r != null && r.getData() != null) {
                Files.delete(r.getData().toPath());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T readSecret(SecretEntry.TypeEnum type, byte[] bytes) {
        switch (type) {
            case DATA:
                return (T) new BinaryDataSecret(bytes);
            case KEY_PAIR:
                return (T) KeyPair.deserialize(bytes);
            case USERNAME_PASSWORD:
                return (T) UsernamePassword.deserialize(bytes);
            default:
                throw new IllegalArgumentException("unknown secret type: " + type);
        }
    }

    private static ApiClient createClient(String baseUrl, String apiKey) {
        ApiClient c = new ConcordApiClient(baseUrl, new OkHttpClient());
        c.setReadTimeout(60000);
        c.setConnectTimeout(10000);
        c.setWriteTimeout(60000);

        c.addDefaultHeader("X-Concord-Trace-Enabled", "true");

        if (apiKey != null) {
            c.setApiKey(apiKey);
        }
        return c;
    }
}
