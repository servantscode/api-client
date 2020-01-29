package org.servantscode.client;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BatchDonationServiceClient extends BaseServiceClient {

    public BatchDonationServiceClient() { super("/rest/donation/batch"); }

    public void createDonations(List<Map<String, Object>> data) {
        Map<String, Object> queryParameters = new HashMap<>();
        queryParameters.put("skipDuplicates", true);
        Response response = post(data, queryParameters);

        if(response.getStatus() == 200)
            System.out.println("Created " + data.size() + " donations.");
        else
            System.err.println("Failed to create donations. Status: " + response.getStatus());
    }
}
