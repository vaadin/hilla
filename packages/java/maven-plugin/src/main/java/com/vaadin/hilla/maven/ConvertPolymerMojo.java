package com.vaadin.hilla.maven;

import org.apache.maven.plugins.annotations.Mojo;

/**
 * A Maven goal that converts Polymer-based source files to Lit.
 */
@Mojo(name = "convert-polymer")
public class ConvertPolymerMojo
        extends com.vaadin.flow.plugin.maven.ConvertPolymerMojo {

}
