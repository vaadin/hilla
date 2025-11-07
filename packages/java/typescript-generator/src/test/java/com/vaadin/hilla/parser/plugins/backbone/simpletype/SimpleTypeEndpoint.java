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
package com.vaadin.hilla.parser.plugins.backbone.simpletype;

import com.vaadin.hilla.parser.testutils.annotations.Endpoint;
import java.math.BigDecimal;
import java.math.BigInteger;

@Endpoint
public class SimpleTypeEndpoint {
    public void doSomething() {
        // no-op
    }

    public int[] getArray() {
        return new int[] { 1, 2, 3 };
    }

    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(0);
    }

    public BigInteger getBigInteger() {
        return BigInteger.valueOf(0);
    }

    public boolean getBoolean() {
        return true;
    }

    public Boolean getBooleanWrapper() {
        return getBoolean();
    }

    public byte getByte() {
        return 0;
    }

    public Byte getByteWrapper() {
        return getByte();
    }

    public char getChar() {
        return 'a';
    }

    public Character getCharWrapper() {
        return getChar();
    }

    public double getDouble() {
        return 0.0;
    }

    public Double getDoubleWrapper() {
        return getDouble();
    }

    public float getFloat() {
        return (float) 0.0;
    }

    public Float getFloatWrapper() {
        return getFloat();
    }

    public int getInteger() {
        return 0;
    }

    public Integer getIntegerWrapper() {
        return getInteger();
    }

    public long getLong() {
        return 0;
    }

    public Long getLongWrapper() {
        return getLong();
    }

    public short getShort() {
        return 0;
    }

    public Short getShortWrapper() {
        return getShort();
    }

    public String getString() {
        return "test";
    };

    protected String getProtectedValue() {
        return "protected";
    }

    protected void setProtectedValue(String protectedValue) {
        // ignore
    }
}
