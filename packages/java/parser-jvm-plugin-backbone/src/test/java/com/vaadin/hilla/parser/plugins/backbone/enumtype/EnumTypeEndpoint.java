/**
 * Copyright 2000-2026 Vaadin Ltd.
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 * See {@literal <https://vaadin.com/commercial-license-and-service-terms>} for the full
 * license.
 */
package com.vaadin.hilla.parser.plugins.backbone.enumtype;

import java.util.List;

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
}
