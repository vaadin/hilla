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

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;

/**
 * The translations live in <code>vaadin-i18n</code> property files on the
 * classpath and are read at runtime, so they are only there when the native
 * image includes them as resources.
 */
public class I18nIT extends AbstractNativeIT {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        open("/i18n");
        waitUntil(driver -> !$(TextFieldElement.class).all().isEmpty());
    }

    @Test
    public void defaultTranslationIsUsed() {
        waitUntil(driver -> "Hello from the classpath"
                .equals($("*").id("greeting").getText()));
    }

    @Test
    public void translationOfTheChosenLanguageIsUsed() {
        $(TextFieldElement.class).id("language").setValue("fi");

        waitUntil(driver -> "Hei luokkapolusta"
                .equals($("*").id("greeting").getText()));
        Assert.assertEquals("fi", $("*").id("resolved-language").getText());
    }

}
