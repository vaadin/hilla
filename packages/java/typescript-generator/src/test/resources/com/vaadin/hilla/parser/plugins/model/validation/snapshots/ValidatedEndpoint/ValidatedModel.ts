import { _getPropertyModel, ArrayModel, Email, makeObjectEmptyValueCreator, Min, NotBlank, NumberModel, ObjectModel, Pattern, Size, StringModel } from "@vaadin/hilla-lit-form";
import type Validated from "./Validated.js";
class ValidatedModel<T extends Validated = Validated> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(ValidatedModel);
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, true, { meta: { annotations: [{ name: "jakarta.persistence.Id" }], javaType: "long" } }));
    }
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get count(): NumberModel {
        return this[_getPropertyModel]("count", (parent, key) => new NumberModel(parent, key, false, { validators: [new Min(1)], meta: { javaType: "int" } }));
    }
    get address(): StringModel {
        return this[_getPropertyModel]("address", (parent, key) => new StringModel(parent, key, true, { validators: [new Email({ message: "that isn't an address" })], meta: { javaType: "java.lang.String" } }));
    }
    get code(): StringModel {
        return this[_getPropertyModel]("code", (parent, key) => new StringModel(parent, key, true, { validators: [new Pattern({ regexp: "[a-z]+", flags: ["CASE_INSENSITIVE"] })], meta: { javaType: "java.lang.String" } }));
    }
    get codes(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("codes", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { validators: [new Size({ min: 1 })], meta: { javaType: "java.lang.String" } }), { meta: { javaType: "java.lang.String[]" } }));
    }
    get tags(): ArrayModel<StringModel> {
        return this[_getPropertyModel]("tags", (parent, key) => new ArrayModel(parent, key, true, (parent, key) => new StringModel(parent, key, true, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }), { validators: [new Size({ min: 1, max: 10 })], meta: { javaType: "java.util.List" } }));
    }
}
export default ValidatedModel;
