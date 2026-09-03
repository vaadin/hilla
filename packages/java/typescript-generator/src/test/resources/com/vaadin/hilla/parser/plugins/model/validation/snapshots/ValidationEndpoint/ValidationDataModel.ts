import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, AssertFalse as AssertFalse_1, AssertTrue as AssertTrue_1, DecimalMax as DecimalMax_1, DecimalMin as DecimalMin_1, Digits as Digits_1, Email as Email_1, Future as Future_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, Max as Max_1, Min as Min_1, Negative as Negative_1, NegativeOrZero as NegativeOrZero_1, NotBlank as NotBlank_1, NotEmpty as NotEmpty_1, NotNull as NotNull_1, Null as Null_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Past as Past_1, Pattern as Pattern_1, Positive as Positive_1, PositiveOrZero as PositiveOrZero_1, Size as Size_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type ValidationData_1 from "./ValidationData.js";
import ValidationDataModel_1 from "./ValidationDataModel.js";
class ValidationDataModel<T extends ValidationData_1 = ValidationData_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ValidationDataModel);
    get assertFalse(): StringModel_1 {
        return this[_getPropertyModel_1]("assertFalse", (parent, key) => new StringModel_1(parent, key, true, { validators: [new AssertFalse_1()], meta: { javaType: "java.lang.String" } }));
    }
    get assertTrue(): StringModel_1 {
        return this[_getPropertyModel_1]("assertTrue", (parent, key) => new StringModel_1(parent, key, true, { validators: [new AssertTrue_1()], meta: { javaType: "java.lang.String" } }));
    }
    get decimalMax(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMax", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new DecimalMax_1({ inclusive: false, value: "0.01" })], meta: { javaType: "double" } }));
    }
    get decimalMin(): NumberModel_1 {
        return this[_getPropertyModel_1]("decimalMin", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new DecimalMin_1("0.01")], meta: { javaType: "double" } }));
    }
    get digits(): StringModel_1 {
        return this[_getPropertyModel_1]("digits", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Digits_1({ integer: 5, fraction: 2 })], meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel_1 {
        return this[_getPropertyModel_1]("email", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Email_1({ message: "foo" })], meta: { javaType: "java.lang.String" } }));
    }
    get future(): StringModel_1 {
        return this[_getPropertyModel_1]("future", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Future_1()], meta: { javaType: "java.time.LocalDate" } }));
    }
    get isNull(): StringModel_1 {
        return this[_getPropertyModel_1]("isNull", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Null_1()], meta: { javaType: "java.lang.String" } }));
    }
    get list(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("list", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { meta: { javaType: "java.lang.String" } }), { validators: [new NotEmpty_1()], meta: { javaType: "java.util.List" } }));
    }
    get max(): NumberModel_1 {
        return this[_getPropertyModel_1]("max", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new Max_1(2)], meta: { javaType: "java.lang.Integer" } }));
    }
    get min(): NumberModel_1 {
        return this[_getPropertyModel_1]("min", (parent, key) => new NumberModel_1(parent, key, true, { validators: [new Min_1({ message: "foo", value: 1 })], meta: { javaType: "java.lang.Integer" } }));
    }
    get negative(): NumberModel_1 {
        return this[_getPropertyModel_1]("negative", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Negative_1()], meta: { javaType: "int" } }));
    }
    get negativeOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("negativeOrZero", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new NegativeOrZero_1()], meta: { javaType: "int" } }));
    }
    get notBlank(): StringModel_1 {
        return this[_getPropertyModel_1]("notBlank", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get notEmpty(): StringModel_1 {
        return this[_getPropertyModel_1]("notEmpty", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotNull_1(), new NotEmpty_1()], meta: { javaType: "java.lang.String" } }));
    }
    get notNull(): StringModel_1 {
        return this[_getPropertyModel_1]("notNull", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotNull_1()], meta: { javaType: "java.lang.String" } }));
    }
    get notNullEntity(): ValidationDataModel_1 {
        return this[_getPropertyModel_1]("notNullEntity", (parent, key) => new ValidationDataModel_1(parent, key, true, { validators: [new NotNull_1()] }));
    }
    get past(): StringModel_1 {
        return this[_getPropertyModel_1]("past", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Past_1()], meta: { javaType: "java.time.LocalDate" } }));
    }
    get pattern(): StringModel_1 {
        return this[_getPropertyModel_1]("pattern", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Pattern_1({ regexp: "\\d+\\..+" })], meta: { javaType: "java.lang.String" } }));
    }
    get positive(): NumberModel_1 {
        return this[_getPropertyModel_1]("positive", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Positive_1()], meta: { javaType: "int" } }));
    }
    get positiveOrZero(): NumberModel_1 {
        return this[_getPropertyModel_1]("positiveOrZero", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new PositiveOrZero_1()], meta: { javaType: "int" } }));
    }
    get size(): StringModel_1 {
        return this[_getPropertyModel_1]("size", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Size_1()], meta: { javaType: "java.lang.String" } }));
    }
    get size1(): StringModel_1 {
        return this[_getPropertyModel_1]("size1", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Size_1({ min: 1 })], meta: { javaType: "java.lang.String" } }));
    }
    get withConstraintsOnSetter(): StringModel_1 {
        return this[_getPropertyModel_1]("withConstraintsOnSetter", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotNull_1(), new NotBlank_1(), new Email_1()], meta: { javaType: "java.lang.String" } }));
    }
    get withGetter(): StringModel_1 {
        return this[_getPropertyModel_1]("withGetter", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get withSetter(): StringModel_1 {
        return this[_getPropertyModel_1]("withSetter", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Email_1(), new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
}
export default ValidationDataModel;
