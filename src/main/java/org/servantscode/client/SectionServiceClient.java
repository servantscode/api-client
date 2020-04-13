package org.servantscode.client;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SectionServiceClient extends BaseServiceClient {

    public SectionServiceClient(int programId) { super(String.format("/rest/program/%d/section", programId)); }

    public Map<String, Object> createSection(Map<String, Object> data) {
        Response response = post(data);

        if(response.getStatus() == 200)
            System.out.println("Created program section: " + data.get("name"));
        else
            System.err.println("Failed to create program section. Status: " + response.getStatus());

        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }

    public Map<String, Object> updateSection(Map<String, Object> data) {
        Response response = put(data);

        if(response.getStatus() == 200)
            System.out.println("Updated program section: " + data.get("name"));
        else
            System.err.println("Failed to update program section. Status: " + response.getStatus());

        return response.readEntity(new GenericType<Map<String, Object>>(){});
    }

    public Map<String, Object> getDefaultSection() {
        Response response = get();

        if(response.getStatus() == 200)
            System.out.println("Got program  sections.");
        else
            System.err.println("Failed to create program section. Status: " + response.getStatus());

        Map<String, Object> respData = response.readEntity(new GenericType<Map<String, Object>>(){});
        List<Object> results = (List<Object>)respData.get("results");
        return results.size()>0 ? (Map<String, Object>)results.get(0): null;
    }
}
