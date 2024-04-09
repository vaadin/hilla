package com.vaadin.hilla.test.reactgrid;

import org.junit.Test;

public class GridWithEntityReferencesIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/grid-entityrefs";
    }

    @Test
    public void expectedColumnsShown() {
        assertColumns("Name", "Street address", "City", "Country",
                "Department");
        assertRow(0, "Alice", "12 Baker St", "London", "UK",
                "{\"id\":1,\"version\":1,\"name\":\"IT\"}");
    }

}
