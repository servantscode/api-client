package org.servantscode.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.servantscode.commons.security.SystemJWTGenerator;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

public class ApiClientFactory {
    private static final Logger LOG = LogManager.getLogger(ApiClientFactory.class);

    private static final Map<String, String> DIRECTORY;

    static {
        DIRECTORY = new HashMap<>();
        DIRECTORY.put("/rest/family", "http://person-svc:8080/rest/family");
        DIRECTORY.put("/rest/parish", "http://parish-svc:8080/rest/parish");
//        DIRECTORY.put("/rest/email", "http://email-svc:8080/rest/email");
    }

    // ----- Singleton -----
    private static ApiClientFactory INSTANCE = new ApiClientFactory();

    private ApiClientFactory() {}

    public static ApiClientFactory instance() {
        return INSTANCE;
    }

    // ----- Public -----
    private String externalPrefix = "http://localhost";
    private boolean internalAccess = false;

    public void setExternalPrefix(String prefix) {
        externalPrefix = prefix;
    }

    public String urlFor(String url) {
        if(internalAccess)
            return DIRECTORY.get(url);
        else
            return externalPrefix + url;
    }

    public String getReferralUrl() {
        return externalPrefix;
    }

    public void authenticateAsSystem() {
        internalAccess = true;
    }

    public String getToken() {
        if(internalAccess)
            return SystemJWTGenerator.generateToken();
        else
            return login("greg@servantscode.org","1234");
    }

    public String login(String email, String password) {
        WebTarget webTarget = ClientBuilder.newClient(new ClientConfig().register(BaseServiceClient.class))
                .target(urlFor("/rest/login"));

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        Invocation.Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
        Response response = invocationBuilder
                .header("referer", ApiClientFactory.instance().getReferralUrl())
                .post(Entity.entity(credentials, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 200)
            LOG.error("Failed to login. Status: " + response.getStatus());

        return response.readEntity(String.class);
    }
}
