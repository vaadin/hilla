import { _getPropertyModel, BooleanModel, Email, makeObjectEmptyValueCreator, NotBlank, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import AbstractEntityModel from "./AbstractEntityModel.js";
import type Address from "./Address.js";
import type Person from "./Person.js";
class PersonModel<T extends Person = Person> extends AbstractEntityModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PersonModel);
    get id(): NumberModel {
        return this[_getPropertyModel]("id", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Long" } }));
    }
    get firstName(): StringModel {
        return this[_getPropertyModel]("firstName", (parent, key) => new StringModel(parent, key, false, { validators: [new NotBlank()], meta: { javaType: "java.lang.String" } }));
    }
    get lastName(): StringModel {
        return this[_getPropertyModel]("lastName", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel {
        return this[_getPropertyModel]("email", (parent, key) => new StringModel(parent, key, true, { validators: [new Email()], meta: { javaType: "java.lang.String" } }));
    }
    get phone(): StringModel {
        return this[_getPropertyModel]("phone", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
    get important(): BooleanModel {
        return this[_getPropertyModel]("important", (parent, key) => new BooleanModel(parent, key, false, { meta: { javaType: "boolean" } }));
    }
    get luckyNumber(): NumberModel {
        return this[_getPropertyModel]("luckyNumber", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
    get addresses(): ObjectModel<Record<string, Address>> {
        return this[_getPropertyModel]("addresses", (parent, key) => new ObjectModel(parent, key, false, { meta: { javaType: "java.util.Map" } }));
    }
    get profilePicture(): StringModel {
        return this[_getPropertyModel]("profilePicture", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get age(): NumberModel {
        return this[_getPropertyModel]("age", (parent, key) => new NumberModel(parent, key, true, { meta: { javaType: "java.lang.Integer" } }));
    }
    get fullName(): StringModel {
        return this[_getPropertyModel]("fullName", (parent, key) => new StringModel(parent, key, false, { meta: { javaType: "java.lang.String" } }));
    }
}
export default PersonModel;
