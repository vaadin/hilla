module dev.hilla.parser.plugins.backbone {
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires io.github.classgraph;
    requires jsr305;
    requires dev.hilla.parser.core;
    requires com.fasterxml.jackson.databind;

    opens dev.hilla.parser.plugins.backbone to dev.hilla.parser.core;
    exports dev.hilla.parser.plugins.backbone;
}
