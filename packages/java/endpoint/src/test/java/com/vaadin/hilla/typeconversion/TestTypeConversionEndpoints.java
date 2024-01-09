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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.vaadin.hilla.Endpoint;

@Endpoint
public class TestTypeConversionEndpoints {
    public int addOneInt(int value) {
        return value + 1;
    }

    public boolean revertBoolean(boolean value) {
        return !value;
    }

    public byte addOneByte(byte value) {
        return (byte) (value + 1);
    }

    public char getChar(char value) {
        return value;
    }

    public short addOneShort(short value) {
        return (short) (value + 1);
    }

    public long addOneLong(long value) {
        return value + 1;
    }

    public float addOneFloat(float value) {
        return value + 1;
    }

    public double addOneDouble(double value) {
        return value + 1;
    }

    public Integer addOneIntBoxed(Integer value) {
        return value == null ? null : value + 1;
    }

    public Boolean revertBooleanBoxed(Boolean value) {
        return value == null ? null : !value;
    }

    public Byte addOneByteBoxed(Byte value) {
        return value == null ? null : (byte) (value + 1);
    }

    public Short addOneShortBoxed(Short value) {
        return value == null ? null : (short) (value + 1);
    }

    public Long addOneLongBoxed(Long value) {
        return value == null ? null : value + 1;
    }

    public Float addOneFloatBoxed(Float value) {
        return value == null ? null : value + 1;
    }

    public Double addOneDoubleBoxed(Double value) {
        return value == null ? null : value + 1;
    }

    public Character getCharBoxed(Character value) {
        return value;
    }

    public String addFooString(String value) {
        return value + "foo";
    }

    public Date addOneDayToDate(Date value) {
        if (value == null) {
            return null;
        }
        Instant plusOneDay = value.toInstant().plus(1, ChronoUnit.DAYS);
        return Date.from(plusOneDay);
    }

    public LocalDate addOneDayLocalDate(LocalDate value) {
        return value.plus(1, ChronoUnit.DAYS);
    }

    public LocalTime addOneHourLocalTime(LocalTime value) {
        return value.plus(1, ChronoUnit.HOURS);
    }

    public LocalDateTime addOneDayOneHourLocalDateTime(LocalDateTime value) {
        return value.plus(1, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS);
    }

    public int[] getAddOneArray(int[] value) {
        return Arrays.stream(value).map(i -> i + 1).toArray();
    }

    public String[] getFooStringArray(String[] value) {
        return Arrays.stream(value).map(s -> s + "-foo").toArray(String[]::new);
    }

    public Object[] getObjectArray(Object[] value) {
        return value;
    }

    public Collection<Integer> addOneIntegerCollection(
            Collection<Integer> value) {
        return value.stream().map(i -> i + 1)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public Collection<Double> addOneDoubleCollection(Collection<Double> value) {
        return value.stream().map(i -> i + 1)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public Set<Integer> addoneIntegerSet(Set<Integer> value) {
        return value.stream().map(i -> i + 1)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public Collection<String> addFooStringCollection(Collection<String> value) {
        List<String> list = new LinkedList<>();
        value.forEach(v -> list.add(v + "foo"));
        return list;
    }

    public Collection<Object> getObjectCollection(Collection<Object> value) {
        return value;
    }

    public TestEnum getNextEnum(TestEnum value) {
        return TestEnum.getTestEnum(value.getValue() + 1);
    }

    public Map<String, String> getFooMapStringString(
            Map<String, String> value) {
        Map<String, String> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> stringStringEntry : value.entrySet()) {
            newMap.put(stringStringEntry.getKey(),
                    stringStringEntry.getValue() + "-foo");
        }
        return newMap;
    }

    public Map<String, Integer> getAddOneMapStringInteger(
            Map<String, Integer> value) {
        Map<String, Integer> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : value.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() + 1);
        }
        return newMap;
    }

    public Map<String, Double> getAddOneMapStringDouble(
            Map<String, Double> value) {
        Map<String, Double> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : value.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() + 1);
        }
        return newMap;
    }

    public Map<String, TestEnum> getNextEnumMapStringEnum(
            Map<String, TestEnum> value) {
        Map<String, TestEnum> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, TestEnum> stringTestEnumEntry : value
                .entrySet()) {
            newMap.put(stringTestEnumEntry.getKey(), TestEnum.getTestEnum(
                    stringTestEnumEntry.getValue().getValue() + 1));
        }
        return newMap;
    }

    public Map<TestEnum, Integer> getAddOneMapEnumInteger(
            Map<TestEnum, Integer> value) {
        Map<TestEnum, Integer> newMap = new LinkedHashMap<>();
        for (Map.Entry<TestEnum, Integer> testEnumStringEntry : value
                .entrySet()) {
            newMap.put(testEnumStringEntry.getKey(),
                    testEnumStringEntry.getValue() + 1);
        }
        return newMap;
    }

    public EnumMap<TestEnum, String> getFooEnumMap(
            EnumMap<TestEnum, String> value) {
        EnumMap<TestEnum, String> enumStringEnumMap = new EnumMap<>(
                TestEnum.class);
        for (Map.Entry<TestEnum, String> entry : value.entrySet()) {
            enumStringEnumMap.put(entry.getKey(), entry.getValue() + "foo");
        }
        return enumStringEnumMap;
    }

    public EnumSet<TestEnum> getNextValueEnumSet(EnumSet<TestEnum> value) {
        EnumSet<TestEnum> enumSet = EnumSet.noneOf(TestEnum.class);
        value.forEach(testEnum -> enumSet
                .add(TestEnum.getTestEnum(testEnum.getValue() + 1)));
        return enumSet;
    }

    public EndpointTestBean getFooBean(EndpointTestBean value) {
        EndpointTestBean newBean = new EndpointTestBean();
        newBean.name = value.name + "-foo";
        newBean.address = value.address + "-foo";
        newBean.age = value.age + 1;
        newBean.isAdmin = !value.isAdmin;
        value.roles.add("User");
        newBean.roles = value.roles;
        newBean.testEnum = TestEnum.getTestEnum(value.testEnum.getValue() + 1);
        newBean.setCustomProperty(value.getCustomProperty() + "-foo");
        return newBean;
    }

    public enum TestEnum {
        FIRST(1), SECOND(2), THIRD(3);

        private final int value;

        TestEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static TestEnum getTestEnum(int value) {
            for (TestEnum testEnum : TestEnum.values()) {
                if (testEnum.value == value) {
                    return testEnum;
                }
            }
            return null;
        }
    }
}
