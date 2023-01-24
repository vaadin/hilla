package dev.hilla.parser.plugins.backbone.simpletype;

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
}
