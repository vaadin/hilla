import { _getPropertyModel as _getPropertyModel_1, ArrayModel as ArrayModel_1, Email as Email_1, makeObjectEmptyValueCreator as makeObjectEmptyValueCreator_1, Min as Min_1, NotBlank as NotBlank_1, NumberModel as NumberModel_1, ObjectModel as ObjectModel_1, Pattern as Pattern_1, Size as Size_1, StringModel as StringModel_1 } from "@vaadin/hilla-lit-form";
import type Validated_1 from "./Validated.js";
class ValidatedModel<T extends Validated_1 = Validated_1> extends ObjectModel_1<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator_1(ValidatedModel);
    get id(): NumberModel_1 {
        return this[_getPropertyModel_1]("id", (parent, key) => new NumberModel_1(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "long" } }));
    }
    get name(): StringModel_1 {
        return this[_getPropertyModel_1]("name", (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }));
    }
    get count(): NumberModel_1 {
        return this[_getPropertyModel_1]("count", (parent, key) => new NumberModel_1(parent, key, false, { validators: [new Min_1(1)], meta: { javaType: "int" } }));
    }
    get address(): StringModel_1 {
        return this[_getPropertyModel_1]("address", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Email_1({ message: "that isn't an address" })], meta: { javaType: "java.lang.String" } }));
    }
    get code(): StringModel_1 {
        return this[_getPropertyModel_1]("code", (parent, key) => new StringModel_1(parent, key, true, { validators: [new Pattern_1({ regexp: "[a-z]+", flags: ["CASE_INSENSITIVE"] })], meta: { javaType: "java.lang.String" } }));
    }
    get codes(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("codes", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { validators: [new Size_1({ min: 1 })], meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }));
    }
    get tags(): ArrayModel_1<StringModel_1> {
        return this[_getPropertyModel_1]("tags", (parent, key) => new ArrayModel_1(parent, key, true, (parent, key) => new StringModel_1(parent, key, true, { validators: [new NotBlank_1()], meta: { javaType: "java.lang.String" } }), { validators: [new Size_1({ min: 1, max: 10 })], meta: { javaType: "java.util.List" } }));
    }
}
export default ValidatedModel;
