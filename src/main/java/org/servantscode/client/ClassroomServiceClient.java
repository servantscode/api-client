package org.servantscode.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.Map;


public class ClassroomServiceClient extends BaseServiceClient {

    public ClassroomServiceClient(int programId, int sectionId) {
        super(String.format("/rest/program/%d/section/%d/classroom", programId, sectionId));
    }

    public Map<String, Object> createClassroom(Map<String, Object> data) {
        Response response = post(data);

        if(response.getStatus() == 200)
            System.out.println("Created program section: " + data.get("name"));
        else
            System.err.println("Failed to create program section. Status: " + response.getStatus());

        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }
}
