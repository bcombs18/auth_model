package org.pac4j.demo.spark;

import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.authorization.authorizer.ProfileAuthorizer;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.CommonProfile;

import java.util.List;

public class CustomAuthorizer extends ProfileAuthorizer<CommonProfile> {

    @Override
    public boolean isAuthorized(final WebContext context, final List<CommonProfile> profiles) {
        return isAnyAuthorized(context, profiles);
    }

    @Override
    public boolean isProfileAuthorized(final WebContext context, final CommonProfile profile) {
        if (profile == null) {
            return false;
        }
        return StringUtils.startsWith(profile.getUsername(), "jle");
    }
}
