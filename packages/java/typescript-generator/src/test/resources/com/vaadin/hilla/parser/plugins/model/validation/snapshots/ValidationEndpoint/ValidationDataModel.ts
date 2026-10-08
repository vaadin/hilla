import { _getPropertyModel, ArrayModel, AssertFalse, AssertTrue, DecimalMax, DecimalMin, Digits, Email, Future, makeObjectEmptyValueCreator, Max, Min, Negative, NegativeOrZero, NotBlank, NotEmpty, NotNull, Null, NumberModel, ObjectModel, Past, Pattern, Positive, PositiveOrZero, Size, StringModel } from "@vaadin/hilla-lit-form";
import type ValidationData from "./ValidationData.js";
import ValidationDataModel_1 from "./ValidationDataModel.js";
class ValidationDataModel<T extends ValidationData = ValidationData> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ValidationDataModel);
    get assertFalse(): StringModel {
        return this[_getPropertyModel]("assertFalse", (parent, key) => new StringModel(parent, key, true, { validators: [new AssertFalse()], meta: { javaType: "java.lang.String" } }));
    }
    get assertTrue(): StringModel {
        return this[_getPropertyModel]("assertTrue", (parent, key) => new StringModel(parent, key, true, { validators: [new AssertTrue()], meta: { javaType: "java.lang.String" } }));
    }
    get decimalMax(): NumberModel {
        return this[_getPropertyModel]("decimalMax", (parent, key) => new NumberModel(parent, key, false, { validators: [new DecimalMax({ inclusive: false, value: "0.01" })], meta: { javaType: "double" } }));
    }
    get decimalMin(): NumberModel {
        return this[_getPropertyModel]("decimalMin", (parent, key) => new NumberModel(parent, key, false, { validators: [new DecimalMin("0.01")], meta: { javaType: "double" } }));
    }
    get digits(): StringModel {
        return this[_getPropertyModel]("digits", (parent, key) => new StringModel(parent, key, true, { validators: [new Digits({ integer: 5, fraction: 2 })], meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel {
        return this[_getPropertyModel]("email", (parent, key) => new StringModel(parent, key, true, { validators: [new Email({ message: "foo" })], meta: { javaType: "java.lang.String" } }));
    }
    get future(): StringModel {
        return this[_getPropertyModel]("future", (parent, key) => new StringModel(parent, key, true, { validators: [new Future()], meta: { javaType: "java.time.LocalDate" } }));
    }
    get isNull(): StringModel {
        return this[_getPropertyModel]("isNull", (parent, key) => new StringModel(parent, key, true, { validators: [new Null()], meta: { javaType: "java.lang.String" } }));
    }
    get list(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("list", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }), { validators: [new NotEmpty()], meta: { javaType: "java.util.List" } }));
    }
    get max(): NumberModel {
        return this[_getPropertyModel]("max", (parent, key) => new NumberModel(parent, key, true, { validators: [new Max(2)], meta: { javaType: "java.lang.Integer" } }));
    }
    get min(): NumberModel {
        return this[_getPropertyModel]("min", (parent, key) => new NumberModel(parent, key, true, { validators: [new Min({ message: "foo", value: 1 })], meta: { javaType: "java.lang.Integer" } }));
    }
    get negative(): NumberModel {
        return this[_getPropertyModel]("negative", (parent, key) => new NumberModel(parent, key, false, { validators: [new Negative()], meta: { javaType: "int" } }));
    }
    get negativeOrZero(): NumberModel {
        return this[_getPropertyModel]("negativeOrZero", (parent, key) => new NumberModel(parent, key, false, { validators: [new NegativeOrZero()], meta: { javaType: "int" } }));
    }
    get notBlank(): StringModel {
        return this[_getPropertyModel]("notBlank", (parent, key) => new StringModel(parent, key, true, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get notEmpty(): StringModel {
        return this[_getPropertyModel]("notEmpty", (parent, key) => new StringModel(parent, key, true, { validators: [new NotNull(), new NotEmpty()], meta: { javaType: "java.lang.String" } }));
    }
    get notNull(): StringModel {
        return this[_getPropertyModel]("notNull", (parent, key) => new StringModel(parent, key, true, { validators: [new NotNull()], meta: { javaType: "java.lang.String" } }));
    }
    get notNullEntity(): ValidationDataModel_1 {
        return this[_getPropertyModel]("notNullEntity", (parent, key) => new ValidationDataModel_1(parent, key, true, { validators: [new NotNull()] }));
    }
    get past(): StringModel {
        return this[_getPropertyModel]("past", (parent, key) => new StringModel(parent, key, true, { validators: [new Past()], meta: { javaType: "java.time.LocalDate" } }));
    }
    get pattern(): StringModel {
        return this[_getPropertyModel]("pattern", (parent, key) => new StringModel(parent, key, true, { validators: [new Pattern({ regexp: "\\d+\\..+" })], meta: { javaType: "java.lang.String" } }));
    }
    get positive(): NumberModel {
        return this[_getPropertyModel]("positive", (parent, key) => new NumberModel(parent, key, false, { validators: [new Positive()], meta: { javaType: "int" } }));
    }
    get positiveOrZero(): NumberModel {
        return this[_getPropertyModel]("positiveOrZero", (parent, key) => new NumberModel(parent, key, false, { validators: [new PositiveOrZero()], meta: { javaType: "int" } }));
    }
    get size(): StringModel {
        return this[_getPropertyModel]("size", (parent, key) => new StringModel(parent, key, true, { validators: [new Size()], meta: { javaType: "java.lang.String" } }));
    }
    get size1(): StringModel {
        return this[_getPropertyModel]("size1", (parent, key) => new StringModel(parent, key, true, { validators: [new Size({ min: 1 })], meta: { javaType: "java.lang.String" } }));
    }
    get withConstraintsOnSetter(): StringModel {
        return this[_getPropertyModel]("withConstraintsOnSetter", (parent, key) => new StringModel(parent, key, true, { validators: [new NotNull(), new NotBlank(), new Email()], meta: { javaType: "java.lang.String" } }));
    }
    get withGetter(): StringModel {
        return this[_getPropertyModel]("withGetter", (parent, key) => new StringModel(parent, key, true, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get withSetter(): StringModel {
        return this[_getPropertyModel]("withSetter", (parent, key) => new StringModel(parent, key, true, { validators: [new Email(), new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
}
export default ValidationDataModel;
