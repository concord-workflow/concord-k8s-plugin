package ca.vanzyl.concord.plugins.inventory;

import com.squareup.okhttp.OkHttpClient;
import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.client.ConcordApiClient;

public class ConcordClientSupport {

    protected final ApiClient client;

    public ConcordClientSupport(String url, String apiKey) {
        this(createClient(url, apiKey));
    }

    public ConcordClientSupport(ApiClient apiClient) {
        this.client = apiClient;
    }

    public static ApiClient createClient(String baseUrl, String apiKey) {

        ApiClient client = new ConcordApiClient(baseUrl, new OkHttpClient());
        client.setReadTimeout(60000);
        client.setConnectTimeout(10000);
        client.setWriteTimeout(60000);

        if (apiKey != null) {
            client.setApiKey(apiKey);
        }

        return client;
    }
}
