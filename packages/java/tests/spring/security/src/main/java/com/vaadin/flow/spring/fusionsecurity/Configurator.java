package com.vaadin.flow.spring.fusionsecurity;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;

@PWA(name = "Spring Security Helper Test Project", shortName = "SSH Test")
@Theme("spring-security-test-app")
public class Configurator implements AppShellConfigurator {

}
