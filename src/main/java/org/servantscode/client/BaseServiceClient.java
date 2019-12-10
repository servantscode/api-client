package org.servantscode.client;

import org.servantscode.commons.client.AbstractServiceClient;

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
}
