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
    public void setLangWithoutCountry_onlyLangWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es", "es-ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountry_langWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es_ES", "es-ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountry_langWithDifferentCountryAvailable_shouldUseLangWithDifferentCountry()
            throws InterruptedException {
        assertTranslations("es_AR", "es-ES", "Nombre", "Direccion");
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
        assertTranslations("", "und", "Name", "Address");
    }

    @Test
    public void setInvalidLang_defaultTranslationAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        assertTranslations("KLINGON", "und", "Name", "Address");
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
