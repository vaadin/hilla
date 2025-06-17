package com.vaadin.hilla;

import jakarta.annotation.Nonnull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
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
