package dev.hilla.test.reactgrid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

public class AutoFormViewIT extends ChromeBrowserTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getTestPath());
    }

    protected String getTestPath() {
        return getRootURL() + "/auto-form";
    }

    @Test
    public void valuesRemainWhenSubmittingInvalidData() {
        getTextField("name").setValue("It's me, Mario");
        getTextField("doctor").setValue("");
        submit();

        // Field values remain
        Assert.assertEquals("It's me, Mario", getTextField("name").getValue());
    }

    @Test
    public void validDataSubmitted() {
        getTextField("name").setValue("John Doe");
        getTextField("doctor").setValue("Evil");
        submit();

        waitUntil(driver -> {
            return $("*").id("form-submitted").getText().equals(
                    "Thank you John Doe, your appointment has been reserved.");

        });
    }

    @Test
    public void emptySubmittedShowsErrors() {
        submit();
        Assert.assertEquals("must not be blank",
                getTextField("name").getPropertyString("errorMessage"));
        Assert.assertEquals("must not be blank",
                getTextField("doctor").getPropertyString("errorMessage"));
    }

    private void submit() {
        $(ButtonElement.class).first().click();
    }

    private TextFieldElement getTextField(String name) {
        return $(TextFieldElement.class).attribute("name", name).first();
    }

}
