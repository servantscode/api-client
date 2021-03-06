package org.servantscode.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;


public class DonationServiceClient extends BaseServiceClient {

    public DonationServiceClient() { super("/rest/donation"); }

    public Map<String, Object> createDonation(Map<String, Object> data) {
        Response response = post(data);

        if(response.getStatus() != 200)
            System.err.println("Failed to create donation. Status: " + response.getStatus());

        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }
}
