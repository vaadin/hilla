package com.vaadin.hilla.devmode.devtools;

import javax.sql.DataSource;

import java.sql.Connection;
import java.util.Optional;

import com.vaadin.hilla.ApplicationContextProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.h2.H2ConsoleProperties;

import com.vaadin.base.devserver.DevToolsInterface;
import com.vaadin.base.devserver.DevToolsMessageHandler;

import elemental.json.Json;
import elemental.json.JsonObject;

public class DevToolsDatabase implements DevToolsMessageHandler {

    private boolean autowired = false;
    @Autowired
    private Optional<H2ConsoleProperties> h2ConsoleProperties;
    @Autowired
    private ObjectProvider<DataSource> dataSource;

    @Override
    public void handleConnect(DevToolsInterface devToolsInterface) {
        initIfNeeded();
        JsonObject data = Json.createObject();

        if (h2ConsoleProperties.isPresent()
                && h2ConsoleProperties.get().getEnabled()) {
            DataSource ds = dataSource.getIfAvailable();
            if (ds != null) {
                JsonObject h2 = Json.createObject();
                h2.put("path", h2ConsoleProperties.get().getPath());
                h2.put("jdbcUrl", getConnectionUrl(ds));
                data.put("h2", h2);
            }
        }
        devToolsInterface.send("devtools-database-init", data);
    }

    private void initIfNeeded() {
        if (!autowired) {
            ApplicationContextProvider.getApplicationContext()
                    .getAutowireCapableBeanFactory().autowireBean(this);
            autowired = true;
        }
    }

    private String getConnectionUrl(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean handleMessage(String command, JsonObject data,
            DevToolsInterface devToolsInterface) {
        return false;
    }

}
