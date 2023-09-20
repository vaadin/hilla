package dev.hilla.parser.plugins.model.validation;

import java.time.LocalDate;
import java.util.List;

import dev.hilla.parser.plugins.model.Endpoint;
import jakarta.validation.constraints.AssertFalse;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Endpoint
public class ValidationEndpoint {
    public ValidationData getValidationData() {
        return new ValidationData();
    }

    public static class ValidationData {
        @AssertFalse
        public String assertFalse;

        @AssertTrue
        public String assertTrue;

        @DecimalMax(value = "0.01", inclusive = false)
        public double decimalMax;

        @DecimalMin("0.01")
        public double decimalMin;

        @Digits(integer = 5, fraction = 2)
        public String digits;

        @Email(message = "foo")
        public String email;

        @Future
        public LocalDate future;

        @Null
        public String isNull;

        @NotEmpty
        public List<String> list;

        @Max(2)
        public Integer max;

        @Min(value = 1, message = "foo")
        public Integer min;

        @Negative
        public int negative;

        @NegativeOrZero
        public int negativeOrZero;

        @NotBlank
        public String notBlank;

        @NotNull
        @NotEmpty
        public String notEmpty;

        @NotNull
        public String notNull;

        @NotNull
        public ValidationData notNullEntity;

        @Past
        public LocalDate past;

        @Pattern(regexp = "\\d+\\..+")
        public String pattern;

        @Positive
        public int positive;

        @PositiveOrZero
        public int positiveOrZero;

        @Size
        public String size;

        @Size(min = 1)
        public String size1;

        private String withConstraintsOnSetter;

        public String getWithConstraintsOnSetter() {
            return withConstraintsOnSetter;
        }

        public void setWithConstraintsOnSetter(
                @NotNull @NotBlank @Email String withConstraintsOnSetter) {
            this.withConstraintsOnSetter = withConstraintsOnSetter;
        }

        @NotBlank
        private String withGetter;

        public String getWithGetter() {
            return this.withGetter;
        }

        @NotBlank
        public String withSetter;

        public void setWithSetter(@Email String withSetter) {
            this.withSetter = withSetter;
        }
    }
}
