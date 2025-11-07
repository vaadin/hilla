/*
 * Copyright 2000-2025 Vaadin Ltd.
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
package com.vaadin.hilla.test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import com.vaadin.testbench.TestBenchElement;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BasicI18NIT extends ChromeBrowserTest {

    private TextFieldElement nameField;

    private TextFieldElement addressField;

    private TextFieldElement languageField;

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        getDriver().get(getRootURL() + "/basic-i18n");
        waitUntil(driver -> $(TextFieldElement.class).all().size() == 3);
        nameField = $(TextFieldElement.class).id("name");
        addressField = $(TextFieldElement.class).id("address");
        languageField = $(TextFieldElement.class).id("language");
    }

    @Test
    public void shouldInitiallyUseDefaultLanguage() {
        Assert.assertEquals("Name", nameField.getLabel());
        Assert.assertEquals("Address", addressField.getLabel());
    }

    @Test
    public void setLangWithoutCountry_onlyLangWithCountryAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        assertTranslations("es", "es", "Name", "Address");
    }

    @Test
    public void setLangWithCountry_langWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es_ES", "es-ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountry_langWithDifferentCountryAvailable_shouldUseLangWithDifferentCountry()
            throws InterruptedException {
        assertTranslations("es_AR", "es-AR", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountryUsingDash_langWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es-ES", "es-ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithoutCountry_langWithoutCountryAvailable_shouldUseLangWithoutCountry()
            throws InterruptedException {
        assertTranslations("fi", "fi", "Nimi", "Osoite");
    }

    @Test
    public void setLangWithCountry_onlyLangWithoutCountryAvailable_shouldUseLangWithoutCountry()
            throws InterruptedException {
        assertTranslations("fi_FI", "fi", "Nimi", "Osoite");
    }

    @Test
    public void setLangEmpty_defaultTranslationAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        // Change away from default empty value to ensure change event
        languageField.setValue("nonempty");
        assertTranslations("", "und", "Name", "Address");
    }

    @Test
    public void setInvalidLang_defaultTranslationAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        assertTranslations("KLINGON", "klingon", "Name", "Address");
    }

    private void assertTranslations(String languageTag,
            String expectedResolvedLanguage, String nameLabel,
            String addressLabel) throws InterruptedException {
        setLanguage(languageTag, expectedResolvedLanguage);
        Assert.assertEquals(nameLabel, nameField.getLabel());
        Assert.assertEquals(addressLabel, addressField.getLabel());
    }

    private void setLanguage(String languageTag,
            String expectedResolvedLanguage) throws InterruptedException {
        languageField.setValue(languageTag);
        TestBenchElement outputSpan = $("span").first();
        waitUntil(ExpectedConditions.textToBePresentInElement(outputSpan,
                "Language: " + languageTag));
        waitUntil(ExpectedConditions.textToBePresentInElement(outputSpan,
                "Resolved Language: " + expectedResolvedLanguage));
    }
}
