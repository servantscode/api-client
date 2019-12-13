package org.servantscode.client;

import org.apache.logging.log4j.ThreadContext;
import org.servantscode.commons.client.AbstractServiceClient;
import org.servantscode.commons.security.OrganizationContext;

import java.util.HashMap;
import java.util.Map;

import static org.servantscode.commons.StringUtils.isEmpty;

public class BaseServiceClient extends AbstractServiceClient {

    private String token = null;

    /*package*/ BaseServiceClient(String service) {
        super(ApiClientFactory.instance().urlFor(service));
    }

    @Override
    public String getReferralUrl() {
        return ApiClientFactory.instance().getReferralUrl();
    }

    @Override
    public String getAuthorization() {
        //If not logged in use default development credentials
        if(isEmpty(token))
            token = ApiClientFactory.instance().getToken();

        return "Bearer " + token;
    }

    @Override
    public Map<String, String> getAdditionalHeaders() {
        HashMap<String, String> headers = new HashMap<>(4);
        headers.put("x-sc-org", OrganizationContext.getOrganization().getHostName());
        headers.put("x-sc-transaction-id", ThreadContext.get("transaction.id"));
        return headers;
    }
}
