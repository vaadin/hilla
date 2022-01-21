package com.vaadin.fusion.parser.plugins.model.validation;

import java.time.LocalDate;
import java.util.List;

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

@Endpoint
public class ValidationEndpoint {
    public ValidationData getValidationData() {
        return new ValidationData();
    }

    public static class ValidationData {
        @AssertFalse
        private String assertFalse;

        @AssertTrue
        private String assertTrue;

        @DecimalMax(value = "0.01", inclusive = false)
        private double decimalMax;

        @DecimalMin("0.01")
        private double decimalMin;

        @Digits(integer = 5, fraction = 2)
        private String digits;

        @Email(message = "foo")
        private String email;

        @Future
        private LocalDate future;

        @Null
        private String isNull;

        @NotEmpty
        private List<String> list;

        @Max(2)
        private Integer max;

        @Min(value = 1, message = "foo")
        private Integer min;

        @Negative
        private int negative;

        @NegativeOrZero
        private int negativeOrZero;

        @NotBlank
        private String notBlank;

        @NotNull
        @NotEmpty
        private String notEmpty;

        @NotNull
        private String notNull;

        @NotNull
        private ValidationData notNullEntity;

        @Past
        private LocalDate past;

        @Pattern(regexp = "\\d+\\..+")
        private String pattern;

        @Positive
        private int positive;

        @PositiveOrZero
        private int positiveOrZero;

        @Size
        private String size;

        @Size(min = 1)
        private String size1;
    }
}
