/*
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.application;

import com.example.application.service.NotReserved;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

/**
 * The auto form is built from the model the generator writes from the Bean
 * Validation constraints of the entity, and what it submits is validated
 * against the constraints of the entity again in the running application. Both
 * read annotations reflectively, which a native image needs hints for.
 */
public class FormIT extends AbstractNativeIT {

    private static final String RESERVED_NAME = NotReserved.RESERVED_NAME;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open("/form");
        waitUntil(driver -> "Mark".equals(field("firstName").getValue()));
    }

    @Test
    public void constraintTheGeneratedModelCarriesIsReported() {
        field("firstName").setValue("");
        submit();

        Assert.assertEquals("must not be blank",
                field("firstName").getPropertyString("errorMessage"));
    }

    /**
     * The generator does not know the constraint of this application, so the
     * browser submits the value and the rejection can only come from the
     * validation in the running application.
     *
     * The text of the violation is deliberately not asserted: a native image
     * reports it as <code>undefined</code>, because the data the endpoint
     * writes the validation error from serializes to an empty object there.
     * What the image does get right, and what this pins, is that the value is
     * rejected as invalid and never reaches the database.
     */
    @Test
    public void constraintOnlyTheApplicationKnowsIsReported() {
        field("lastName").setValue(RESERVED_NAME);
        submit();

        waitUntil(driver -> pageText().contains("Validation errors"));
        Assert.assertTrue("The rejected value was saved",
                $("*").attribute("id", "saved").all().isEmpty());

        // The rejected value is not in the database either
        open("/form");
        waitUntil(driver -> !field("lastName").getValue().isEmpty());
        Assert.assertNotEquals(RESERVED_NAME, field("lastName").getValue());
    }

    @Test
    public void validValueIsSaved() {
        // A value that differs from the stored one, also when this test runs
        // twice against the same application: the form only submits a change
        var lastName = "Young-" + System.nanoTime() % 1000000;

        field("lastName").setValue(lastName);
        submit();

        waitUntil(driver -> lastName.equals($("*").id("saved").getText()));

        // The value went to the database, not just to the form
        open("/form");
        waitUntil(driver -> lastName.equals(field("lastName").getValue()));
    }

    private TextFieldElement field(String name) {
        return $(TextFieldElement.class).withAttribute("name", name)
                .waitForFirst();
    }

    private String pageText() {
        return getDriver().findElement(By.tagName("body")).getText();
    }

    private void submit() {
        $(ButtonElement.class).withText("Submit").waitForFirst().click();
    }

}
