import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, AssertFalse as AssertFalse_1, AssertTrue as AssertTrue_1, DecimalMax as DecimalMax_1, DecimalMin as DecimalMin_1, Digits as Digits_1, Email as Email_1, Future as Future_1, Max as Max_1, Min as Min_1, Negative as Negative_1, NegativeOrZero as NegativeOrZero_1, NotBlank as NotBlank_1, NotEmpty as NotEmpty_1, NotNull as NotNull_1, Null as Null_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Past as Past_1, Pattern as Pattern_1, Positive as Positive_1, PositiveOrZero as PositiveOrZero_1, Size as Size_1, StringModel as StringModel_1 } from "@hilla/form";
import FormEntityModel_1 from "./FormEntityModel";
import type FormValidationConstraints_1 from "./FormValidationConstraints";
class FormValidationConstraintsModel<T extends FormValidationConstraints_1 = FormValidationConstraints_1> extends ObjectModel_1<T> {
    static createEmptyValue: () => FormValidationConstraints_1;
    get list(): ArrayModel_1<string, StringModel_1> {
        return this[_getPropertyModel_1]("list", ArrayModel_1, [true, StringModel_1, [true], new NotEmpty_1()]) as ArrayModel_1<string, StringModel_1>;
    }
    get email(): StringModel_1 {
        return this[_getPropertyModel_1]("email", StringModel_1, [true, new Email_1({ message: "foo" })]) as StringModel_1;
    }
    get isNull(): StringModel_1 {
        return this[_getPropertyModel_1]("isNull", StringModel_1, [true, new Null_1()]) as StringModel_1;
    }
    get notNull(): StringModel_1 {
        return this[_getPropertyModel_1]("notNull", StringModel_1, [true, new NotNull_1()]) as StringModel_1;
    }
    get notEmpty(): StringModel_1 {
        return this[_getPropertyModel_1]("notEmpty", StringModel_1, [true, new NotEmpty_1(), new NotNull_1()]) as StringModel_1;
    }
    get notNullEntity(): FormEntityModel_1 {
        return this[_getPropertyModel_1]("notNullEntity", FormEntityModel_1, [true]) as FormEntityModel_1;
    }
    get notBlank(): StringModel_1 {
        return this[_getPropertyModel_1]("notBlank", StringModel_1, [true, new NotBlank_1()]) as StringModel_1;
    }
    get assertTrue(): StringModel_1 {
        return this[_getPropertyModel_1]("assertTrue", StringModel_1, [true, new AssertTrue_1()]) as StringModel_1;
    }
    get assertFalse(): StringModel_1 {
        return this[_getPropertyModel_1]("assertFalse", StringModel_1, [true, new AssertFalse_1()]) as StringModel_1;
    }
    get min(): NumberModel_1 {
        return this[_getPropertyModel_1]("min", NumberModel_1, [true, new Min_1({ value: 1, message: "foo" })]) as NumberModel_1;
    }
    get max(): NumberModel_1 {
        return this[_getPropertyModel_1]("max", NumberModel_1, [true, new Max_1(2)]) as NumberModel_1;
    }
    get decimalMin(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMin", NumberModel_1, [false, new DecimalMin_1("0.01")]) as NumberModel_1;
    }
    get decimalMax(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMax", NumberModel_1, [false, new DecimalMax_1({ value: "0.01", inclusive: false })]) as NumberModel_1;
    }
    get negative(): NumberModel_1 {
        return this[_getPropertyModel_1]("negative", NumberModel_1, [false, new Negative_1()]) as NumberModel_1;
    }
    get negativeOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("negativeOrZero", NumberModel_1, [false, new NegativeOrZero_1()]) as NumberModel_1;
    }
    get positive(): NumberModel_1 {
        return this[_getPropertyModel_1]("positive", NumberModel_1, [false, new Positive_1()]) as NumberModel_1;
    }
    get positiveOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("positiveOrZero", NumberModel_1, [false, new PositiveOrZero_1()]) as NumberModel_1;
    }
    get size(): StringModel_1 {
        return this[_getPropertyModel_1]("size", StringModel_1, [true, new Size_1()]) as StringModel_1;
    }
    get size1(): StringModel_1 {
        return this[_getPropertyModel_1]("size1", StringModel_1, [true, new Size_1({ min: 1 })]) as StringModel_1;
    }
    get digits(): StringModel_1 {
        return this[_getPropertyModel_1]("digits", StringModel_1, [true, new Digits_1({ integer: 5, fraction: 2 })]) as StringModel_1;
    }
    get past(): StringModel_1 {
        return this[_getPropertyModel_1]("past", StringModel_1, [true, new Past_1()]) as StringModel_1;
    }
    get future(): StringModel_1 {
        return this[_getPropertyModel_1]("future", StringModel_1, [true, new Future_1()]) as StringModel_1;
    }
    get pattern(): StringModel_1 {
        return this[_getPropertyModel_1]("pattern", StringModel_1, [true, new Pattern_1({ regexp: "\\d+\\..+" })]) as StringModel_1;
    }
}
export default FormValidationConstraintsModel;
