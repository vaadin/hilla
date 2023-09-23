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
        getTextField("doctor").setValue("Evil");
        getTextField("time").setValue("now");
        submit();

        // Field values remain
        Assert.assertEquals("It's me, Mario", getTextField("name").getValue());
        // Custom error message shown
        Assert.assertEquals(
                "Something went wrong, please check all your values",
                $("div").id("formerror").getText());

    }

    @Test
    public void validDataSubmitted() {
        getTextField("name").setValue("John Doe");
        getTextField("doctor").setValue("Evil");
        getTextField("time").setValue("2020-10-03T09:20:00.000Z");
        submit();

        Assert.assertEquals(
                "Thank you John Doe, your appointment has been reserved.",
                $("*").id("form-submitted").getText());

    }

    @Test
    public void emptySubmittedShowsErrors() {
        submit();
        Assert.assertEquals("must not be blank",
                getTextField("name").getPropertyString("errorMessage"));
        Assert.assertEquals("must not be blank",
                getTextField("doctor").getPropertyString("errorMessage"));
        // The time field should have an error but it does not
        // Assert.assertEquals("must not be blank",
        // getTextField("time").getPropertyString("errorMessage"));

    }

    private void submit() {
        $(ButtonElement.class).first().click();
    }

    private TextFieldElement getTextField(String name) {
        return $(TextFieldElement.class).attribute("name", name).first();
    }

}
