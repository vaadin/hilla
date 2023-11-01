package dev.hilla.crud;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class TestObject {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String name;
    private LocalDate localDate;
    private LocalTime localTime;
    private LocalDateTime localDateTime;
    private Boolean booleanValue;
    private int intValue;
    private Integer nullableIntValue;
    private long longValue;
    private Long nullableLongValue;
    private float floatValue;
    private Float nullableFloatValue;
    private double doubleValue;
    private Double nullableDoubleValue;
    private TestEnum enumValue;

    @OneToOne
    private NestedObject nestedObject;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean aBoolean) {
        this.booleanValue = aBoolean;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public Integer getNullableIntValue() {
        return nullableIntValue;
    }

    public void setNullableIntValue(Integer nullableIntValue) {
        this.nullableIntValue = nullableIntValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.longValue = longValue;
    }

    public Long getNullableLongValue() {
        return nullableLongValue;
    }

    public void setNullableLongValue(Long nullableLongValue) {
        this.nullableLongValue = nullableLongValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.floatValue = floatValue;
    }

    public Float getNullableFloatValue() {
        return nullableFloatValue;
    }

    public void setNullableFloatValue(Float nullableFloatValue) {
        this.nullableFloatValue = nullableFloatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.doubleValue = doubleValue;
    }

    public Double getNullableDoubleValue() {
        return nullableDoubleValue;
    }

    public void setNullableDoubleValue(Double nullableDoubleValue) {
        this.nullableDoubleValue = nullableDoubleValue;
    }

    public TestEnum getEnumValue() {
        return enumValue;
    }

    public void setEnumValue(TestEnum testEnum) {
        this.enumValue = testEnum;
    }

    public NestedObject getNestedObject() {
        return nestedObject;
    }

    public void setNestedObject(NestedObject nestedObject) {
        this.nestedObject = nestedObject;
    }
}
