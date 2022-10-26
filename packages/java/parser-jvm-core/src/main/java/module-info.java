module dev.hilla.parser.core {
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires dev.hilla.parser.utils;
    requires io.github.classgraph;
    requires io.swagger.v3.core;
    requires io.swagger.v3.oas.models;
    requires jsr305;
    requires jakarta.annotation;
    requires org.slf4j;

    opens dev.hilla.parser.core to com.fasterxml.jackson.databind;
    exports dev.hilla.parser.core;
    exports dev.hilla.parser.models;
}
