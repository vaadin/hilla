package com.vaadin.flow.spring.fusionsecuritycontextpath;

public class SecurityIT
        extends com.vaadin.flow.spring.fusionsecurity.SecurityIT {

    @Override
    protected String getUrlMappingBasePath() {
        return "/vaadin";
    }

}
