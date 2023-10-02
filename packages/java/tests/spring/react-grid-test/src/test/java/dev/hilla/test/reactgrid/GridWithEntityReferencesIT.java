package dev.hilla.test.reactgrid;

import org.junit.Test;

public class GridWithEntityReferencesIT extends AbstractGridTest {

    protected String getTestPath() {
        return getRootURL() + "/grid-entityrefs";
    }

    @Test
    public void expectedColumnsShown() {
        assertColumns("Name", "Street address", "City", "Country", "Allergies");
        assertRow(0, "Alice", "12 Baker St", "London", "UK",
                "[object Object],[object Object]");
    }

}
