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
package com.vaadin.hilla.parser.plugins.backbone.enumtype;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;

@Endpoint
public class EnumTypeEndpoint {
    private EnumEntity value = EnumEntity.ENUM1;

    public EnumEntity echoEnum(EnumEntity value) {
        return value;
    }

    public List<EnumEntity> echoListEnum(List<EnumEntity> enumList) {
        return enumList;
    }

    public EnumEntity getEnum() {
        return value;
    }

    public void setEnum(EnumEntity value) {
        this.value = value;
    }

    public RenamedEnum echoRenamed(RenamedEnum value) {
        return value;
    }

    public NumberedEnum echoNumbered(NumberedEnum value) {
        return value;
    }

    public LabelledEnum echoLabelled(LabelledEnum value) {
        return value;
    }

    /**
     * Constants Jackson writes under a name of their own, one of which
     * TypeScript only accepts as a string.
     */
    public enum RenamedEnum {
        @JsonProperty("very-low")
        VERY_LOW, @JsonProperty("it's high")
        HIGH, UNCHANGED;

        // A public field of the enum class which is not one of its constants
        public static final RenamedEnum DEFAULT = UNCHANGED;
    }

    /**
     * Constants with a label of their own, which Jackson writes them as unless
     * the mapper is told otherwise.
     */
    public enum LabelledEnum {
        LOW, HIGH;

        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase()
                    + " priority";
        }
    }

    /**
     * Constants Jackson writes as numbers. The generated enum holds strings, so
     * they keep their names, which is a known limitation: the server sends 0
     * and 1, not these names. What is pinned here is only that such an enum
     * does not break the generation.
     */
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    public enum NumberedEnum {
        FIRST, SECOND
    }

    public enum EnumEntity {
        ENUM1(1),
        ENUM2(2),
        ENUM_2(2),
        HELLO_WORLD(3),
        _HELLO(4),
        MANY_MANY_WORDS(5);

        private final int value;

        EnumEntity(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
