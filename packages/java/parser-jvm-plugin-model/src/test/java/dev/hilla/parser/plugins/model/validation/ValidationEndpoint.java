package dev.hilla.parser.plugins.model.validation;

import java.time.LocalDate;
import java.util.List;

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
