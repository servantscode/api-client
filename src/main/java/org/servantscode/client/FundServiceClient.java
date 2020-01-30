package org.servantscode.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class FundServiceClient extends BaseServiceClient {

    public FundServiceClient() { super("/rest/fund"); }

    public Map<String, Object> createFund(Map<String, Object> data) {
        Response response = post(data);

        if(response.getStatus() == 200)
            System.out.println("Created fund: " + data.get("name"));
        else
            System.err.println("Failed to create fund. Status: " + response.getStatus());

        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }

    public int getFundId(String fundName) {
        Map<String, Object> params = new HashMap<>(8);
        params.put("search", fundName);

        Response response = get(params);
        if(response.getStatus() != 200)
            throw new RuntimeException("Could not query for fund by name: " + fundName);

        Map<String, Object> resp = response.readEntity(new GenericType<Map<String, Object>>(){});
        List<Map<String, Object>> results = (List<Map<String, Object>>) resp.get("results");
        results = results.stream().filter(r -> r.get("name").equals(fundName)).collect(Collectors.toList());

        return results.isEmpty()? 0: (int)results.get(0).get("id");
    }
}
