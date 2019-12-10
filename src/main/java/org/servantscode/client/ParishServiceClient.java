package org.servantscode.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Map;


public class ParishServiceClient extends BaseServiceClient {

    public ParishServiceClient() { super("/rest/parish"); }

    public Map<String, Object> getParish(int id) {
        Response response = get("/" + id);
        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }

    public Map<String, Object> getParishForOrg(int id) {
        Response response = get("/org/" + id);
        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }
}
