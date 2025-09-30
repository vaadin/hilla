/*
 * Copyright 2000-2022 Vaadin Ltd.
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
package com.vaadin.hilla.typeconversion;

import org.junit.Test;

public class BeanConversionTest extends BaseTypeConversionTest {

    @Test
    public void should_ConvertToBean_When_ReceiveBeanObject() {
        String inputValue = "{\"name\":\"mybean\",\"address\":\"myaddress\","
                + "\"age\":10,\"isAdmin\":true,\"testEnum\":\"FIRST\","
                + "\"roles\":[\"Admin\"], \"customProperty\": \"customValue\"}";
        // Jackson 3 uses alphabetical property ordering by default
        String expectedValue = "{\"address\":\"myaddress-foo\"," + "\"age\":11,"
                + "\"customProperty\":\"customValue-foo\","
                + "\"isAdmin\":false," + "\"name\":\"mybean-foo\","
                + "\"roles\":[\"Admin\",\"User\"],"
                + "\"testEnum\":\"SECOND\"}";
        assertEqualExpectedValueWhenCallingMethod("getFooBean", inputValue,
                expectedValue);
    }
}
