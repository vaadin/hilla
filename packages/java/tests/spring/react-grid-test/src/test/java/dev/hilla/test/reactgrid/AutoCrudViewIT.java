package dev.hilla.test.reactgrid;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.testbench.TestBenchElement;

public class AutoCrudViewIT extends AbstractGridTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
    }

    protected String getTestPath() {
        return getRootURL() + "/auto-crud";
    }

    @Test
    public void headerShown() {
        List<TestBenchElement> h2s = $("h2").all();
        Assert.assertTrue(
                h2s.stream().filter(h2 -> h2.getText().equals("My crud"))
                        .findFirst().isPresent());
    }

}
