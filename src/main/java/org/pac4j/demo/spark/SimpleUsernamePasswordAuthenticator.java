package org.pac4j.demo.spark;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.core.util.CommonHelper;
import org.pac4j.core.util.Pac4jConstants;

public class SimpleUsernamePasswordAuthenticator implements Authenticator {

    @Override
    public void validate(Credentials credentials, WebContext webContext) {
        if(credentials == null) {
            throw new CredentialsException("No credentials provided");
        }

        final var creds = (UsernamePasswordCredentials) credentials;
        var username = creds.getUsername();
        var password = creds.getPassword();
        if(CommonHelper.isBlank(username) || CommonHelper.isBlank(password)) {
            throw new CredentialsException("Username and password cannot be left blank");
        }

        final var profile = new CommonProfile();
        profile.setId(username);
        profile.addAttribute(Pac4jConstants.USERNAME, username);
        credentials.setUserProfile(profile);
    }
}
