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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

/**
 * The auto form is built from the model the generator writes from the Bean
 * Validation constraints of the entity, and the value it submits is validated
 * against those same constraints on the server. Both read annotations
 * reflectively, which a native image needs hints for.
 */
public class FormIT extends AbstractNativeIT {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open("/form");
        waitUntil(driver -> "Mark".equals(field("firstName").getValue()));
    }

    @Test
    public void constraintOfTheEntityIsReported() {
        field("firstName").setValue("");
        submit();

        Assert.assertEquals("must not be blank",
                field("firstName").getPropertyString("errorMessage"));
    }

    @Test
    public void sizeConstraintOfTheEntityIsReported() {
        field("lastName").setValue("Young-Young-Young-Young");
        submit();

        Assert.assertEquals("size must be between 0 and 20",
                field("lastName").getPropertyString("errorMessage"));
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

    private void submit() {
        $(ButtonElement.class).withText("Submit").waitForFirst().click();
    }

}
