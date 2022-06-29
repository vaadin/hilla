package com.vaadin.flow.connect;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Meta;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.server.PWA;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Meta(name = "foo", content = "bar")
@PWA(name = "My App", shortName = "app")
public class AppShell implements AppShellConfigurator {
    private TitleService titleService;

    public AppShell(@Autowired TitleService titleService) {
        this.titleService = titleService;
    }

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setPageTitle(titleService.getTitle());
    }

    @Service
    public static class TitleService {
        public String getTitle() {
            return "titleRetrievedFromAService";
        }
    }
}
