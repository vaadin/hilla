package com.vaadin.fusion.parser.plugins.backbone.validation;

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
        public String getAssertFalse() {
            return null;
        }

        @AssertTrue
        public String getAssertTrue() {
            return null;
        }

        @DecimalMax(value = "0.01", inclusive = false)
        public double getDecimalMax() {
            return 0;
        }

        @DecimalMin("0.01")
        public double getDecimalMin() {
            return 0;
        }

        @Digits(integer = 5, fraction = 2)
        public String getDigits() {
            return null;
        }

        @Email(message = "foo")
        public String getEmail() {
            return null;
        }

        @Future
        public LocalDate getFuture() {
            return null;
        }

        @Null
        public String getIsNull() {
            return null;
        }

        @NotEmpty
        public List<String> getList() {
            return null;
        }

        @Max(2)
        public Integer getMax() {
            return null;
        }

        @Min(value = 1, message = "foo")
        public Integer getMin() {
            return null;
        }

        @Negative
        public int getNegative() {
            return 0;
        }

        @NegativeOrZero
        public int getNegativeOrZero() {
            return 0;
        }

        @NotBlank
        public String getNotBlank() {
            return null;
        }

        @NotNull
        @NotEmpty
        public String getNotEmpty() {
            return null;
        }

        @NotNull
        public String getNotNull() {
            return null;
        }

        @NotNull
        public ValidationData getNotNullEntity() {
            return null;
        }

        @Past
        public LocalDate getPast() {
            return null;
        }

        @Pattern(regexp = "\\d+\\..+")
        public String getPattern() {
            return null;
        }

        @Positive
        public int getPositive() {
            return 0;
        }

        @PositiveOrZero
        public int getPositiveOrZero() {
            return 0;
        }

        @Size
        public String getSize() {
            return null;
        }

        @Size(min = 1)
        public String getSize1() {
            return null;
        }
    }
}
