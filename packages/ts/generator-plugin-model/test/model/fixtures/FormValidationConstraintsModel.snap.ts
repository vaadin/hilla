import { _getPropertyModel, ArrayModel, AssertFalse, AssertTrue, DecimalMax, DecimalMin, Digits, Email, Future, makeObjectEmptyValueCreator, Max, Min, Negative, NegativeOrZero, NotBlank, NotEmpty, NotNull, Null, NumberModel, ObjectModel, Past, Pattern, Positive, PositiveOrZero, Size, StringModel } from "@vaadin/hilla-lit-form";
import FormEntityModel from "./FormEntityModel.js";
import type FormValidationConstraints from "./FormValidationConstraints.js";
class FormValidationConstraintsModel<T extends FormValidationConstraints = FormValidationConstraints> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(FormValidationConstraintsModel);
    get list(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("list", (parent, key) => new ArrayModel(parent, key, false, (parent, key) => new StringModel(parent, key, true), { validators: [new NotEmpty()] }));
    }
    get email(): StringModel {
        return this[_getPropertyModel]("email", (parent, key) => new StringModel(parent, key, false, { validators: [new Email({ message: "foo" })] }));
    }
    get isNull(): StringModel {
        return this[_getPropertyModel]("isNull", (parent, key) => new StringModel(parent, key, false, { validators: [new Null()] }));
    }
    get notNull(): StringModel {
        return this[_getPropertyModel]("notNull", (parent, key) => new StringModel(parent, key, false, { validators: [new NotNull()] }));
    }
    get notEmpty(): StringModel {
        return this[_getPropertyModel]("notEmpty", (parent, key) => new StringModel(parent, key, false, { validators: [new NotEmpty(), new NotNull()] }));
    }
    get notNullEntity(): FormEntityModel {
        return this[_getPropertyModel]("notNullEntity", (parent, key) => new FormEntityModel(parent, key, false, { validators: [new NotNull()] }));
    }
    get notBlank(): StringModel {
        return this[_getPropertyModel]("notBlank", (parent, key) => new StringModel(parent, key, false, { validators: [new NotBlank()] }));
    }
    get assertTrue(): StringModel {
        return this[_getPropertyModel]("assertTrue", (parent, key) => new StringModel(parent, key, false, { validators: [new AssertTrue()] }));
    }
    get assertFalse(): StringModel {
        return this[_getPropertyModel]("assertFalse", (parent, key) => new StringModel(parent, key, false, { validators: [new AssertFalse()] }));
    }
    get min(): NumberModel {
        return this[_getPropertyModel]("min", (parent, key) => new NumberModel(parent, key, false, { validators: [new Min({ value: 1, message: "foo" })] }));
    }
    get max(): NumberModel {
        return this[_getPropertyModel]("max", (parent, key) => new NumberModel(parent, key, false, { validators: [new Max(2)] }));
    }
    get decimalMin(): NumberModel {
        return this[_getPropertyModel]("decimalMin", (parent, key) => new NumberModel(parent, key, false, { validators: [new DecimalMin("0.01")] }));
    }
    get decimalMax(): NumberModel {
        return this[_getPropertyModel]("decimalMax", (parent, key) => new NumberModel(parent, key, false, { validators: [new DecimalMax({ value: "0.01", inclusive: false })] }));
    }
    get negative(): NumberModel {
        return this[_getPropertyModel]("negative", (parent, key) => new NumberModel(parent, key, false, { validators: [new Negative()] }));
    }
    get negativeOrZero(): NumberModel {
        return this[_getPropertyModel]("negativeOrZero", (parent, key) => new NumberModel(parent, key, false, { validators: [new NegativeOrZero()] }));
    }
    get positive(): NumberModel {
        return this[_getPropertyModel]("positive", (parent, key) => new NumberModel(parent, key, false, { validators: [new Positive()] }));
    }
    get positiveOrZero(): NumberModel {
        return this[_getPropertyModel]("positiveOrZero", (parent, key) => new NumberModel(parent, key, false, { validators: [new PositiveOrZero()] }));
    }
    get size(): StringModel {
        return this[_getPropertyModel]("size", (parent, key) => new StringModel(parent, key, false, { validators: [new Size()] }));
    }
    get size1(): StringModel {
        return this[_getPropertyModel]("size1", (parent, key) => new StringModel(parent, key, false, { validators: [new Size({ min: 1 })] }));
    }
    get digits(): StringModel {
        return this[_getPropertyModel]("digits", (parent, key) => new StringModel(parent, key, false, { validators: [new Digits({ integer: 5, fraction: 2 })] }));
    }
    get past(): StringModel {
        return this[_getPropertyModel]("past", (parent, key) => new StringModel(parent, key, false, { validators: [new Past()] }));
    }
    get future(): StringModel {
        return this[_getPropertyModel]("future", (parent, key) => new StringModel(parent, key, false, { validators: [new Future()] }));
    }
    get pattern(): StringModel {
        return this[_getPropertyModel]("pattern", (parent, key) => new StringModel(parent, key, false, { validators: [new Pattern({ regexp: "\\d+\\..+" })] }));
    }
}
export default FormValidationConstraintsModel;
