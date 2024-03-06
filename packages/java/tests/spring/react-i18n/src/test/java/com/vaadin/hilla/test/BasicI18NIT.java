package com.vaadin.hilla.test;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.flow.testutil.ChromeBrowserTest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        assertTranslations("es", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountry_langWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es_ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountry_langWithDifferentCountryAvailable_shouldUseLangWithDifferentCountry()
            throws InterruptedException {
        assertTranslations("es_AR", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithCountryUsingDash_langWithCountryAvailable_shouldUseLangWithCountry()
            throws InterruptedException {
        assertTranslations("es-ES", "Nombre", "Direccion");
    }

    @Test
    public void setLangWithoutCountry_langWithoutCountryAvailable_shouldUseLangWithoutCountry()
            throws InterruptedException {
        assertTranslations("fi", "Nimi", "Osoite");
    }

    @Test
    public void setLangWithCountry_onlyLangWithoutCountryAvailable_shouldUseLangWithoutCountry()
            throws InterruptedException {
        assertTranslations("fi_FI", "Nimi", "Osoite");
    }

    @Test
    public void setLangEmpty_defaultTranslationAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        assertTranslations("", "Name", "Address");
    }

    @Test
    public void setInvalidLang_defaultTranslationAvailable_shouldUseDefaultTranslation()
            throws InterruptedException {
        assertTranslations("KLINGON", "Name", "Address");
    }

    private void assertTranslations(String languageTag, String nameLabel,
            String addressLabel) throws InterruptedException {
        setLanguage(languageTag);
        Assert.assertEquals(nameLabel, nameField.getLabel());
        Assert.assertEquals(addressLabel, addressField.getLabel());
    }

    private void setLanguage(String languageTag) throws InterruptedException {
        languageField.setValue(languageTag);
        Thread.sleep(50);
    }
}
