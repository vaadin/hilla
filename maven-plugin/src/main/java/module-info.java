module com.vaadin.fusion.maven {
    requires maven.plugin.api;
    requires maven.plugin.annotations;
    requires maven.project;
    requires com.vaadin.fusion.parser.core;
    requires io.swagger.v3.oas.models;

    exports com.vaadin.fusion.maven;
}
