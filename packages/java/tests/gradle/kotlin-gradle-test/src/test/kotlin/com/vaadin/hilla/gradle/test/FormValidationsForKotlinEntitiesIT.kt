package com.vaadin.hilla.gradle.test

import com.vaadin.flow.component.button.testbench.ButtonElement
import com.vaadin.flow.component.textfield.testbench.TextFieldElement
import com.vaadin.flow.testutil.ChromeBrowserTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class FormValidationsForKotlinEntitiesIT : ChromeBrowserTest() {

    @Before
    override fun setup() {
        super.setup()
        getDriver().get(testPath)
        `$`(ButtonElement::class.java).withId("save").waitForFirst()
    }

    override fun getTestPath(): String = "$rootURL/person-form"

    @Test
    fun using_NotBlank_on_firstNameField_should_makeItRequired() {
        val saveButton = getButton("save")
        saveButton.click()
        val firstnameField = getTextField("firstname")
        Assert.assertEquals("must not be blank",
            firstnameField.getPropertyString("errorMessage"))

        firstnameField.sendKeys("John")
        saveButton.click()
        Assert.assertEquals("", firstnameField.getPropertyString("errorMessage"))
    }

    @Test
    fun using_Valid_on_emailField_shouldValidateEmail_but_ShouldNot_makeItRequired() {
        val saveButton = getButton("save")
        saveButton.click()
        val emailField = getTextField("email")
        Assert.assertNull(emailField.getPropertyString("errorMessage"))

        emailField.sendKeys("invalid")
        saveButton.focus()
        waitUntil { emailField.getPropertyString("errorMessage") != null }
        Assert.assertEquals("must be a well-formed email address",
            emailField.getPropertyString("errorMessage"))

        emailField.sendKeys("valid@foofle.co")
        saveButton.focus()
        waitUntil { emailField.getPropertyString("errorMessage").isEmpty() }
        Assert.assertEquals("", emailField.getPropertyString("errorMessage"))
    }

    private fun getTextField(id: String): TextFieldElement = `$`(TextFieldElement::class.java).id(id)


    private fun getButton(id: String): ButtonElement = `$`(ButtonElement::class.java).id(id)
}
