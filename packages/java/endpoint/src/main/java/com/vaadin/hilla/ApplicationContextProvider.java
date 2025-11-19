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
package com.vaadin.hilla;

import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
    private static ApplicationContext applicationContext;
    private static final List<Consumer<ApplicationContext>> pendingActions = new ArrayList<>();

    @Override
    public void setApplicationContext(
            @Nonnull ApplicationContext applicationContext)
            throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
        pendingActions.forEach(action -> action.accept(applicationContext));
        pendingActions.clear();
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Allows scheduling actions that should be run when the application context
     * is available, or to run them immediately if the context is already
     * available and still active.
     *
     * @param action
     *            the action to be scheduled or run immediately
     */
    public static void runOnContext(Consumer<ApplicationContext> action) {
        if (applicationContext == null
                || (applicationContext instanceof ConfigurableApplicationContext
                        && !((ConfigurableApplicationContext) applicationContext)
                                .isActive())) {
            pendingActions.add(action);
        } else {
            action.accept(applicationContext);
        }
    }
}
