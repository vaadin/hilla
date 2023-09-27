import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, AssertFalse as AssertFalse_1, AssertTrue as AssertTrue_1, DecimalMax as DecimalMax_1, DecimalMin as DecimalMin_1, Digits as Digits_1, Email as Email_1, Future as Future_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, Max as Max_1, Min as Min_1, Negative as Negative_1, NegativeOrZero as NegativeOrZero_1, NotBlank as NotBlank_1, NotEmpty as NotEmpty_1, NotNull as NotNull_1, Null as Null_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Past as Past_1, Pattern as Pattern_1, Positive as Positive_1, PositiveOrZero as PositiveOrZero_1, Size as Size_1, StringModel as StringModel_1 } from "@hilla/form";
import FormEntityModel_1 from "./FormEntityModel.js";
import type FormValidationConstraints_1 from "./FormValidationConstraints.js";
class FormValidationConstraintsModel<T extends FormValidationConstraints_1 = FormValidationConstraints_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(FormValidationConstraintsModel);
    get list(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("list", (parent, key) => new ArrayModel_1(parent, key, false, (parent, key) => new StringModel_1(parent, key, true), { validators: [new NotEmpty_1()] }));
    }
    get email(): StringModel_1 {
        return this[_getPropertyModel_1]("email", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Email_1({ message: "foo" })] }));
    }
    get isNull(): StringModel_1 {
        return this[_getPropertyModel_1]("isNull", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Null_1()] }));
    }
    get notNull(): StringModel_1 {
        return this[_getPropertyModel_1]("notNull", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotNull_1()] }));
    }
    get notEmpty(): StringModel_1 {
        return this[_getPropertyModel_1]("notEmpty", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotEmpty_1(), new NotNull_1()] }));
    }
    get notNullEntity(): FormEntityModel_1 {
        return this[_getPropertyModel_1]("notNullEntity", (parent, key) => new FormEntityModel_1(parent, key, false));
    }
    get notBlank(): StringModel_1 {
        return this[_getPropertyModel_1]("notBlank", (parent, key) => new StringModel_1(parent, key, false, { validators: [new NotBlank_1()] }));
    }
    get assertTrue(): StringModel_1 {
        return this[_getPropertyModel_1]("assertTrue", (parent, key) => new StringModel_1(parent, key, false, { validators: [new AssertTrue_1()] }));
    }
    get assertFalse(): StringModel_1 {
        return this[_getPropertyModel_1]("assertFalse", (parent, key) => new StringModel_1(parent, key, false, { validators: [new AssertFalse_1()] }));
    }
    get min(): NumberModel_1 {
        return this[_getPropertyModel_1]("min", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Min_1({ value: 1, message: "foo" })] }));
    }
    get max(): NumberModel_1 {
        return this[_getPropertyModel_1]("max", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Max_1(2)] }));
    }
    get decimalMin(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMin", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new DecimalMin_1("0.01")] }));
    }
    get decimalMax(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMax", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new DecimalMax_1({ value: "0.01", inclusive: false })] }));
    }
    get negative(): NumberModel_1 {
        return this[_getPropertyModel_1]("negative", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Negative_1()] }));
    }
    get negativeOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("negativeOrZero", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new NegativeOrZero_1()] }));
    }
    get positive(): NumberModel_1 {
        return this[_getPropertyModel_1]("positive", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Positive_1()] }));
    }
    get positiveOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("positiveOrZero", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new PositiveOrZero_1()] }));
    }
    get size(): StringModel_1 {
        return this[_getPropertyModel_1]("size", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Size_1()] }));
    }
    get size1(): StringModel_1 {
        return this[_getPropertyModel_1]("size1", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Size_1({ min: 1 })] }));
    }
    get digits(): StringModel_1 {
        return this[_getPropertyModel_1]("digits", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Digits_1({ integer: 5, fraction: 2 })] }));
    }
    get past(): StringModel_1 {
        return this[_getPropertyModel_1]("past", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Past_1()] }));
    }
    get future(): StringModel_1 {
        return this[_getPropertyModel_1]("future", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Future_1()] }));
    }
    get pattern(): StringModel_1 {
        return this[_getPropertyModel_1]("pattern", (parent, key) => new StringModel_1(parent, key, false, { validators: [new Pattern_1({ regexp: "\\d+\\..+" })] }));
    }
}
export default FormValidationConstraintsModel;
