package com.vaadin.hilla.test.reactgrid;

import com.vaadin.testbench.parallel.Browser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

// Some tests modify the number of items, so they need to not run in parallel.
@RunWith(BlockJUnit4ClassRunner.class)
public class GridUseDataProviderHookIT extends AbstractGridTest {

    @Before
    public void setup() throws Exception {
        setDesiredCapabilities(Browser.CHROME.getDesiredCapabilities());
        super.setup();
    }

    @Test
    public void filteringWorks_and_refreshingDataProvider_keepsTheAppliedFilter() {
        setFilterLastName("car");
        assertRowCount(1);
        assertName(0, "Abigail", "Carter");

        setFilterLastName("Nel");
        assertRowCount(2);
        assertName(0, "Oscar", "Nelson");
        assertName(1, "Leo", "Nelson");

        refresh();

        assertRowCount(2);
        assertName(0, "Oscar", "Nelson");
        assertName(1, "Leo", "Nelson");

        setFilterLastName("");
        assertRowCount(50);
    }

    @Test
    public void sortingWorks_and_refreshingDataProvider_keepsTheAppliedSort() {
        assertFirstName(0, "Alice");
        assertFirstName(1, "Bob");
        assertFirstName(2, "Charlie");
        assertFirstName(3, "David");

        sortByColumn(0); // sort by firstName ascending
        assertFirstName(0, "Abigail");
        assertFirstName(1, "Alice");
        assertFirstName(2, "Benjamin");
        assertFirstName(3, "Bob");

        sortByColumn(0); // sort by firstName descending
        assertFirstName(0, "Zack");
        assertFirstName(1, "Yasmine");
        assertFirstName(2, "Xander");
        assertFirstName(3, "Xander");

        refresh();

        assertFirstName(0, "Zack");
        assertFirstName(1, "Yasmine");
        assertFirstName(2, "Xander");
        assertFirstName(3, "Xander");
    }

    @Test
    public void afterAddingNewItem_refreshingDataProvider_loadsNewItem() {
        assertRowCount(50);

        addPerson();

        try {
            refresh();

            setFilterLastName("Person");
            assertRowCount(1);
            assertName(0, "New", "Person");

            setFilterLastName("");
            assertRowCount(51);

        } finally {
            removePerson();
        }

        refresh();
        assertRowCount(50);
    }

    private void setFilterLastName(String filter) {
        $(TextFieldElement.class).id("filter").setValue(filter);
    }

    private void refresh() {
        var refreshButton = $(ButtonElement.class).all().stream()
                .filter(button -> button.getText().equals("Refresh"))
                .findFirst();
        if (refreshButton.isPresent()) {
            refreshButton.get().click();
        } else {
            Assert.fail("Refresh button not found");
        }
    }

    private void addPerson() {
        var refreshButton = $(ButtonElement.class).all().stream()
                .filter(button -> button.getText().equals("Add Person"))
                .findFirst();
        if (refreshButton.isPresent()) {
            refreshButton.get().click();
        } else {
            Assert.fail("Add Person button not found");
        }
    }

    private void removePerson() {
        var refreshButton = $(ButtonElement.class).all().stream()
                .filter(button -> button.getText().equals("Remove Person"))
                .findFirst();
        if (refreshButton.isPresent()) {
            refreshButton.get().click();
        } else {
            Assert.fail("Remove Person button not found");
        }
    }

}
