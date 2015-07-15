package org.storymaker.app;

public class Constants {
    public static final String ASSET_EULA = "EULA";
    public static final String PREFERENCE_EULA_ACCEPTED = "eula.accepted";
    public static final String PREFERENCES_EULA = "eula";
    public static final String PREFERENCE_ANALYTICS_OPTIN = "analytics.optin";
    public static final String PREFERENCES_ANALYTICS = "analytics";
    public static final String PREFERENCES_WP_REGISTERED = "wp.registered";
    public static final String DEFAULT_SERVER_URL = "https://api.storymaker.org/"; // FIXME we should not use api in here, but prepend it programatically.  we will need to migrate peoples settings if they have api. saved in prefs
}
