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

import java.util.HashMap;
import java.util.Map;

public class SecretsClient {

    public static final String DEFAULT_API_KEY = "auBy4eDWrKWsyhiDp3AQiw";

    private final ApiClient client;

    public SecretsClient(ApiClient apiClient) {
        this.client = apiClient;
    }

    public SecretsClient(String baseUrl) {
        this.client = createClient(baseUrl, DEFAULT_API_KEY, null);
    }

    public void setGithubKey(String githubKey) {
        this.client.addDefaultHeader("X-Hub-Signature", githubKey);
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

    private static ApiClient createClient(String baseUrl, String apiKey, String gitHubKey) {
        ApiClient c = new ConcordApiClient(baseUrl, new OkHttpClient());
        c.setReadTimeout(60000);
        c.setConnectTimeout(10000);
        c.setWriteTimeout(60000);

        c.addDefaultHeader("X-Concord-Trace-Enabled", "true");

        if (apiKey != null) {
            c.setApiKey(apiKey);
        }

        if (gitHubKey != null) {
            c.addDefaultHeader("X-Hub-Signature", gitHubKey);
        }

        return c;
    }
}
