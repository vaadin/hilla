/*
 * Copyright 2000-2021 Vaadin Ltd.
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
package com.vaadin.fusion.parser.plugins.backbone.enumtype;

import java.util.List;

@Endpoint
public class EnumTypeEndpoint {
    public enum EnumEntity {
        ENUM1(1), ENUM2(2), ENUM_2(2), HELLO_WORLD(3), _HELLO(
                4), MANY_MANY_WORDS(5);

        private final int value;

        EnumEntity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private EnumEntity value = EnumEntity.ENUM1;

    public EnumEntity getEnum() {
        return value;
    }

    public void setEnum(EnumEntity value) {
        this.value = value;
    }

    public EnumEntity echoEnum(EnumEntity value) {
        return value;
    }

    public List<EnumEntity> echoListEnum(List<EnumEntity> enumList) {
        return enumList;
    }
}
