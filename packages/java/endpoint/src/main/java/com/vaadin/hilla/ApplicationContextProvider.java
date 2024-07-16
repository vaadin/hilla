package com.vaadin.hilla;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public static void runOnContext(Consumer<ApplicationContext> action) {
        if (applicationContext == null) {
            pendingActions.add(action);
        } else {
            action.accept(applicationContext);
        }
    }
}
