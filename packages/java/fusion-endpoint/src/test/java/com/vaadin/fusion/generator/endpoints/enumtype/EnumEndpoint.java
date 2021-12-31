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
package com.vaadin.fusion.generator.endpoints.enumtype;

import java.util.List;

import com.vaadin.fusion.Endpoint;

@Endpoint
public class EnumEndpoint {
    public enum MyEnum {
        ENUM1(1), ENUM2(2), ENUM_2(2), HELLO_WORLD(3), _HELLO(
                4), MANY_MANY_WORDS(5);

        private final int value;

        MyEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private MyEnum value = MyEnum.ENUM1;

    public MyEnum getEnum() {
        return value;
    }

    public void setEnum(MyEnum value) {
        this.value = value;
    }

    public MyEnum echoEnum(MyEnum value) {
        return value;
    }

    public List<MyEnum> echoListEnum(List<MyEnum> enumList) {
        return enumList;
    }
}
