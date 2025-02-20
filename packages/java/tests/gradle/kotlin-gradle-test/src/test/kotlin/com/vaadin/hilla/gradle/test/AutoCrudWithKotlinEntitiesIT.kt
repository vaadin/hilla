package com.vaadin.hilla.gradle.test

import com.vaadin.flow.component.grid.testbench.GridElement
import com.vaadin.flow.testutil.ChromeBrowserTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openqa.selenium.WebDriver

class AutoCrudWithKotlinEntitiesIT : ChromeBrowserTest() {

    private var grid: GridElement? = null

    @Before
    override fun setup() {
        super.setup()
        getDriver().get(testPath)
        grid = `$`(GridElement::class.java).waitForFirst()
        waitUntil { _: WebDriver? ->
            val prop = grid!!.getProperty("_lastVisibleIndex")
            prop != null
        }
    }

    override fun getTestPath(): String {
        return "$rootURL/persons"
    }

    // Validating that a service extending CrudRepositoryService/ListService is working with Gradle
    @Test
    fun autoCrud_is_working() {
        assertFirstName(0, "Aaron")
        assertFirstName(1, "Agnes")
        assertFirstName(2, "Alejandro")
        assertFirstName(3, "Anne")
    }

    private fun assertFirstName(row: Int, firstName: String) {
        Assert.assertEquals(
            firstName,
            grid!!.getCell(row, getFirstNameColumn()).text
        )
    }

    private fun getFirstNameColumn(): Int {
        return 0
    }
}
