package com.vaadin.hilla.parser.plugins.model.javatypes;

import com.vaadin.hilla.parser.plugins.model.Endpoint;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@Endpoint
public class JavaTypeEndpoint {
    public JavaTypeTestEntity getTestEntity() {
        return new JavaTypeTestEntity();
    }

    public static class CustomEntity {
        public String value;
    }

    public static class JavaTypeTestEntity {
        public boolean aBoolean;
        public Boolean aNullableBoolean;
        public byte aByte;
        public Byte aNullableByte;
        public char aChar;
        public Character aNullableChar;
        public double aDouble;
        public Double aNullableDouble;
        public float aFloat;
        public Float aNullableFloat;
        public int aInt;
        public Integer aNullableInt;
        public long aLong;
        public Long aNullableLong;
        public short aShort;
        public Short aNullableShort;

        public String aString;
        public Date aDate;
        public LocalDate aLocalDate;
        public LocalTime aLocalTime;
        public LocalDateTime aLocalDateTime;

        public String[] aStringArray;
        public byte[] aByteArray;

        public List<String> aStringList;

        public CustomEntity aCustomEntity;
    }
}
