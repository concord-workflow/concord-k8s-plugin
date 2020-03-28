package ca.vanzyl.concord.plugins.inventory;

import com.walmartlabs.concord.ApiClient;
import com.walmartlabs.concord.client.InventoriesApi;
import com.walmartlabs.concord.client.InventoryDataApi;
import com.walmartlabs.concord.client.InventoryEntry;

import javax.inject.Named;
import java.util.Map;

@Named
public class ConcordInventoryClient extends ConcordClientSupport {

    public ConcordInventoryClient(String url, String apiKey) {
        this(createClient(url, apiKey));
    }

    public ConcordInventoryClient(ApiClient apiClient) {
        super(apiClient);
    }

    public void createOrUpdate(String orgName, String inventoryName) throws Exception {

        new InventoriesApi(client).createOrUpdate(orgName, new InventoryEntry()
                .setName(inventoryName)
                .setOrgName(orgName)
                .setVisibility(InventoryEntry.VisibilityEnum.PUBLIC));
    }

    public void putItem(String orgName, String inventoryName, String id, String inventoryJson) throws Exception {
        InventoryDataApi dataApi = new InventoryDataApi(client);
        dataApi.data(orgName, inventoryName, inventoryName + "/" + id, inventoryJson);
    }

    public Map<String, Object> getItem(String orgName, String inventoryName, String id) throws Exception {
        InventoryDataApi dataApi = new InventoryDataApi(client);
        return (Map<String, Object>) dataApi.get(orgName, inventoryName, inventoryName + "/" + id, true);
    }

    public Object getAllItems(String orgName, String inventoryName) throws Exception {
        InventoryDataApi dataApi = new InventoryDataApi(client);
        return dataApi.get(orgName, inventoryName, inventoryName, false);
    }
}
