package dev.hilla.push;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import reactor.core.scheduler.Schedulers;

@WebListener
public class EngineIoCleanup implements ServletContextListener {

    private EngineIoHandler engineIoHandler;

    public EngineIoCleanup() {
        // This is used by everything else than Spring Boot
    }

    public EngineIoCleanup(EngineIoHandler engineIoHandler) {
        this.engineIoHandler = engineIoHandler;
        // This is used by Spring Boot
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (engineIoHandler != null) {
            engineIoHandler.cleanup();
        }
    }
}
