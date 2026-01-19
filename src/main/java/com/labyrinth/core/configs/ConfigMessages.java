package com.labyrinth.core.configs;

public class ConfigMessages extends ConfigBase {

    private static ConfigMessages INSTANCE;

    private ConfigMessages() {
        super("messages");
    }

    public static ConfigMessages getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigMessages();
        }
        return INSTANCE;
    }

    public static class SCROLLS {

        public static final String PORTALS_LOCATION_SET = getInstance().getString("scrolls.portals.location_set", "You have set the first location for this portal scroll.");
        public static final String PORTALS_CREATED = getInstance().getString("scrolls.portals.created", "You have created a portal!");
    }
}
