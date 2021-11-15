package com.vaadin.fusion.parser.plugins.backbone.simpletype;

@Endpoint
public class SimpleTypeEndpoint {
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

    public short getShort() {
        return 0;
    }

    public Short getShortWrapper() {
        return getShort();
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

    public float getFloat() {
        return (float) 0.0;
    }

    public Float getFloatWrapper() {
        return getFloat();
    }

    public double getDouble() {
        return 0.0;
    }

    public Double getDoubleWrapper() {
        return getDouble();
    }

    public char getChar() {
        return 'a';
    }

    public Character getCharWrapper() {
        return getChar();
    }

    public String getString() {
        return "test";
    }

    public int[] getArray() {
        return new int[]{1, 2, 3};
    }
}
