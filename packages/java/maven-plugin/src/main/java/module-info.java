module com.vaadin.fusion.maven {
    requires maven.plugin.api;
    requires maven.plugin.annotations;
    requires maven.project;
    requires jsr305;
    requires com.vaadin.fusion.parser.core;
    requires com.vaadin.fusion.parser.plugins.backbone;
    requires org.apache.commons.io;
    requires io.swagger.v3.oas.models;
    requires com.fasterxml.jackson.core;

    exports com.vaadin.fusion.maven;
}
