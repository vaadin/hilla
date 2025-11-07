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
package com.vaadin.hilla.test.reactgrid;

import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.TestBenchElement;
import org.junit.Test;

public class ReadOnlyGridCustomFilterIT extends AbstractGridTest {

    @Test
    public void findsMatches() {
        setFilterFullName("car");
        assertRowCount(2);
        assertName(0, "Abigail", "Carter");
        assertName(1, "Oscar", "Nelson");

        setFilterLastName("Nel");
        assertRowCount(1);
        assertName(0, "Oscar", "Nelson");

        setFilterFullName("");
        assertRowCount(2);
        assertName(0, "Leo", "Nelson");
        assertName(1, "Oscar", "Nelson");

        setFilterLastName("");
        assertRowCount(50);
    }

    private void setFilterFullName(String filter) {
        $(TextFieldElement.class).id("filter").setValue(filter);
    }

    private void setFilterLastName(String filter) {
        TestBenchElement cont = grid.getHeaderCellContent(1, 1);
        TextFieldElement textFieldElement = cont.$(TextFieldElement.class)
                .first();
        textFieldElement.setValue(filter);
    }

}
