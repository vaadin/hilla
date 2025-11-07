/*
 * Copyright 2000-2025 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
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
