package org.servantscode.commons.security;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;

public class SCSecurityContext implements SecurityContext {

    private final UriInfo uriInfo;
    private final DecodedJWT jwt;

    public SCSecurityContext(UriInfo uriInfo, DecodedJWT jwt)  {
        this.uriInfo = uriInfo;
        this.jwt = jwt;
    }

    @Override
    public Principal getUserPrincipal() {
        return jwt::getSubject;
    }

    @Override
    public boolean isUserInRole(String role) {
        Claim claim = jwt.getClaim("role");
        if(claim == null)
            return false;

        return claim.asString().equalsIgnoreCase(role);
    }

    @Override
    public boolean isSecure() {
        return uriInfo.getAbsolutePath().toString().startsWith("https");
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }
}
