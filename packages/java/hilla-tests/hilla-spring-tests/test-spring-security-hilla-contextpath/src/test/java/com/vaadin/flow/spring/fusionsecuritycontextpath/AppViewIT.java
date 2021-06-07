package com.vaadin.flow.spring.fusionsecuritycontextpath;

public class AppViewIT extends com.vaadin.flow.spring.fusionsecurity.AppViewIT {

    @Override
    protected String getRootURL() {
        return super.getRootURL() + "/context";
    }

}
