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
        $(TextFieldElement.class).waitForFirst();
        nameField = $(TextFieldElement.class).id("name");
        addressField = $(TextFieldElement.class).id("address");
        languageField = $(TextFieldElement.class).id("language");
    }

    @Test
    public void shouldInitiallyUseDefaultLanguage() {
        String name = nameField.getLabel();
        Assert.assertEquals("Name", name);
        Assert.assertEquals("Address", addressField.getLabel());
    }

    @Test
    public void setLangWithoutCountry_onlyLangWithCountryAvailable_shouldUseLangWithCountry() {
        languageField.setValue("es");
        Assert.assertEquals("Nombre", nameField.getLabel());
        Assert.assertEquals("Direccion", addressField.getLabel());
    }

    @Test
    public void setLangWithCountry_langWithCountryAvailable_shouldUseLangWithCountry() {
        languageField.setValue("es_ES");
        Assert.assertEquals("Nombre", nameField.getLabel());
        Assert.assertEquals("Direccion", addressField.getLabel());
    }

    @Test
    public void setLangWithCountry_langWithDifferentCountryAvailable_shouldUseLangWithDifferentCountry() {
        languageField.setValue("es_AR");
        Assert.assertEquals("Nombre", nameField.getLabel());
        Assert.assertEquals("Direccion", addressField.getLabel());
    }

    @Test
    public void setLangWithCountryUsingDash_langWithCountryAvailable_shouldUseLangWithCountry() {
        languageField.setValue("es-ES");
        Assert.assertEquals("Nombre", nameField.getLabel());
        Assert.assertEquals("Direccion", addressField.getLabel());
    }

    @Test
    public void setLangWithoutCountry_langWithoutCountryAvailable_shouldUseLangWithoutCountry() {
        languageField.setValue("fi");
        Assert.assertEquals("Nimi", nameField.getLabel());
        Assert.assertEquals("Osoite", addressField.getLabel());
    }

    @Test
    public void setLangWithCountry_onlyLangWithoutCountryAvailable_shouldUseLangWithoutCountry() {
        languageField.setValue("fi_FI");
        Assert.assertEquals("Nimi", nameField.getLabel());
        Assert.assertEquals("Osoite", addressField.getLabel());
    }

    @Test
    public void setLangEmpty_defaultTranslationAvailable_shouldUseDefaultTranslation() {
        languageField.setValue("");
        Assert.assertEquals("Name", nameField.getLabel());
        Assert.assertEquals("Address", addressField.getLabel());
    }

    @Test
    public void setInvalidLang_defaultTranslationAvailable_shouldUseDefaultTranslation() {
        languageField.setValue("KLINGON");
        Assert.assertEquals("Name", nameField.getLabel());
        Assert.assertEquals("Address", addressField.getLabel());
    }
}
