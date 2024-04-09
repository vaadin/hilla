package com.vaadin.hilla.internal.hotswap;

public class HotSwapConfigurationProperties {
    /**
     * Whether the endpoint changes hot-reload is enabled or not.
     */
    private boolean enabled = true;

    /**
     * The interval to poll for endpoint changes in seconds.
     */
    private int pollInterval = 5;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(int pollInterval) {
        this.pollInterval = pollInterval;
    }
}
