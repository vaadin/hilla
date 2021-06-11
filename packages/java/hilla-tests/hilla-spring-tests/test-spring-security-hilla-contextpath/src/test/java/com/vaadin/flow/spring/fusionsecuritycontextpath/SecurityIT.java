package com.vaadin.flow.spring.fusionsecuritycontextpath;

public class SecurityIT extends com.vaadin.flow.spring.fusionsecurity.SecurityIT {

    @Override
    protected String getRootURL() {
        return super.getRootURL() + "/context";
    }

}
