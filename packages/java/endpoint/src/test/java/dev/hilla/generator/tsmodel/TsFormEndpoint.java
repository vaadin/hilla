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
package dev.hilla.generator.tsmodel;

import javax.annotation.Nonnull;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import dev.hilla.Endpoint;

@Endpoint
public class TsFormEndpoint {

    public MyEntity getEntity() {
        return new MyEntity();
    }

    public String stringNullable() {
        return "";
    }

    public static class MyEntityId {
        @NotNull
        Long Id;
    }

    public static class MyBaz extends MyEntityId {
        String lorem;
        Integer ipsum;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Id {

    }

    public static class MyEntity extends MyEntityId {
        @Id
        Long myId;
        String foo;
        MyBaz bar;
        List<MyBaz> baz;
        Boolean boolWrapper;
        boolean bool;
        @NotEmpty
        List<String> list;
        @Email(message = "foo")
        String email;
        @Null
        String isNull;
        @NotNull
        String notNull;
        @NotNull
        @NotEmpty
        String notEmpty;
        @NotNull
        MyEntity notNullEntity;
        @NotBlank
        String notBlank;
        @AssertTrue
        String assertTrue;
        @AssertFalse
        String assertFalse;
        @Min(value = 1, message = "foo")
        Integer min;
        @Max(2)
        Integer max;
        @DecimalMin("0.01")
        double decimalMin;
        @DecimalMax(value = "0.01", inclusive = false)
        double decimalMax;
        @Negative
        int negative;
        @NegativeOrZero
        int negativeOrZero;
        @Positive
        int positive;
        @PositiveOrZero
        int positiveOrZero;
        @Size
        String size;
        @Size(min = 1)
        String size1;
        @Digits(integer = 5, fraction = 2)
        String digits;
        @Past
        LocalDate past;
        @Future
        LocalDate future;
        LocalTime localTime;
        @Pattern(regexp = "\\d+\\..+")
        String pattern;
        List<MyEntity> children;
        String[] stringArray;
        Number[][] numberMatrix;
        MyEntity[][] entityMatrix;
        Map<String, String> stringMap;
        Map<String, MyBaz> entityMap;
        Optional<String> optionalString;
        Optional<MyEntity> optionalEntity;
        Optional<List<Optional<String>>> optionalList;
        Optional<Optional<String>[][]> optionalMatrix;
        @Nonnull
        String nonNullableString;
        @Nonnull
        List<Optional<String>> nonNullableList;
        @Nonnull
        Optional<String>[][] nonNullableMatrix;
        Map<String, List<String>> mapWithList;
        Map<List<String>, Map<String, List<MyEntity>>> complexMap;
        List<List<Map<String, List<String>>>> nestedArrays;
    }
}
