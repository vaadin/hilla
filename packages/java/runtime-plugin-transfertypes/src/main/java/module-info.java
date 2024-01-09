module com.vaadin.hilla.runtime.transfertypes {
    requires jsr305;
    requires spring.data.commons;
    requires jakarta.validation;
    requires jakarta.annotation;

    exports com.vaadin.hilla.runtime.transfertypes;
    exports com.vaadin.hilla.mappedtypes;
}
