package org.servantscode.client;

import org.apache.logging.log4j.ThreadContext;
import org.servantscode.commons.Organization;
import org.servantscode.commons.client.AbstractServiceClient;
import org.servantscode.commons.security.OrganizationContext;

import java.util.HashMap;
import java.util.Map;

import static org.servantscode.commons.StringUtils.isEmpty;
import static org.servantscode.commons.StringUtils.isSet;

public class BaseServiceClient extends AbstractServiceClient {

    private static String token = null;

    /*package*/ BaseServiceClient(String service) {
        super(ApiClientFactory.instance().urlFor(service));
    }

    public static void login(String email, String password) {
        token = ApiClientFactory.instance().login(email, password);
    }

    @Override
    public String getReferralUrl() {
        return ApiClientFactory.instance().getReferralUrl();
    }

    @Override
    public String getAuthorization() {
        if (isSet(token))
            return "Bearer " + token;

        //If not already logged in get token from the ClientFactory
        return "Bearer " + ApiClientFactory.instance().getToken();
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        HashMap<String, String> headers = new HashMap<>(4);
        Organization org = OrganizationContext.getOrganization();
        if(org != null)
            headers.put("x-sc-org", OrganizationContext.getOrganization().getHostName());
        headers.put("x-sc-transaction-id", ThreadContext.get("transaction.id"));
        return headers;
    }
}
