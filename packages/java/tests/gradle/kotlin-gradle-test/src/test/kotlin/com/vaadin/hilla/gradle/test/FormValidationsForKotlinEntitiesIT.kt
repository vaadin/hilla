package com.vaadin.hilla.gradle.test

import com.vaadin.flow.component.button.testbench.ButtonElement
import com.vaadin.flow.component.textfield.testbench.TextFieldElement
import com.vaadin.flow.testutil.ChromeBrowserTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import java.util.logging.Level

class FormValidationsForKotlinEntitiesIT : ChromeBrowserTest() {

    @Before
    override fun setup() {
        super.setup()
        getDriver().get(testPath)
        try {
            `$`(ButtonElement::class.java).withId("save").waitForFirst()
        } catch (e: TimeoutException) {
            printPageDiagnostics()
            throw e
        }
    }

    // Temporary: on Windows CI the page is sometimes still blank here, without
    // even the layout rendered. Record the browser console so the cause shows
    // up in the build log.
    override fun updateHeadlessChromeOptions(chromeOptions: ChromeOptions) {
        super.updateHeadlessChromeOptions(chromeOptions)
        val logPrefs = LoggingPreferences()
        logPrefs.enable(LogType.BROWSER, Level.ALL)
        chromeOptions.setCapability("goog:loggingPrefs", logPrefs)
    }

    private fun printPageDiagnostics() {
        val report = StringBuilder("=== Page diagnostics for $testPath ===\n")
        try {
            report.append(executeScript(PAGE_STATE_SCRIPT)).append('\n')
        } catch (e: Exception) {
            report.append("Could not read the page state: $e\n")
        }
        report.append("--- Browser console ---\n")
        try {
            driver.manage().logs().get(LogType.BROWSER).forEach {
                report.append("${it.timestamp} ${it.level} ${it.message}\n")
            }
        } catch (e: Exception) {
            report.append("Could not read the browser console: $e\n")
        }
        println(report.append("=== End of page diagnostics ==="))
    }

    override fun getTestPath(): String = "$rootURL/person-form"

    @Test
    fun using_NotBlank_on_firstNameField_should_makeItRequired() {
        val saveButton = getButton("save")
        saveButton.click()
        val firstnameField = getTextField("firstname")
        Assert.assertEquals("must not be blank",
            firstnameField.getPropertyString("errorMessage"))

        // Wait for the invalid-state re-render to settle before typing;
        // otherwise the shadow-DOM <input> can be briefly non-interactable.
        waitUntil { firstnameField.getPropertyBoolean("invalid") == true }
        firstnameField.focus()
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

    companion object {
        private const val PAGE_STATE_SCRIPT = """
            const outlet = document.getElementById('outlet');
            const nav = performance.getEntriesByType('navigation')[0];
            const resources = performance.getEntriesByType('resource').map(r =>
                '  ' + r.name + ' status=' + r.responseStatus +
                ' start=' + Math.round(r.startTime) + 'ms duration=' + Math.round(r.duration) +
                'ms size=' + r.transferSize);
            return [
                'url: ' + location.href,
                'readyState: ' + document.readyState,
                'title: ' + document.title,
                'navigation: status=' + (nav && nav.responseStatus) +
                    ' duration=' + (nav && Math.round(nav.duration)) + 'ms',
                'time since navigation start: ' + Math.round(performance.now()) + 'ms',
                'window.Vaadin keys: ' + (window.Vaadin ? Object.keys(window.Vaadin).join(', ') : 'undefined'),
                'scripts: ' + Array.from(document.scripts).map(s => s.src || '(inline)').join(', '),
                'outlet: ' + (outlet ? outlet.innerHTML.substring(0, 2000) : 'missing'),
                'body: ' + document.body.innerHTML.substring(0, 2000),
                'resources (' + resources.length + '):',
            ].concat(resources).join('\n');
        """
    }
}
