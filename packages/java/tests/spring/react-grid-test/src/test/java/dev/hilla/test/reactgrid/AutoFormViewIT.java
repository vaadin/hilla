package dev.hilla.test.reactgrid;

import com.vaadin.flow.component.textfield.testbench.IntegerFieldElement;
import com.vaadin.flow.component.textfield.testbench.NumberFieldElement;
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
        getIntegerField("age").setValue("54");
        getNumberField("rating").setValue("7.8");
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

    private IntegerFieldElement getIntegerField(String name) {
        return $(IntegerFieldElement.class).attribute("name", name).first();
    }

    private NumberFieldElement getNumberField(String name) {
        return $(NumberFieldElement.class).attribute("name", name).first();
    }
}
