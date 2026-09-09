import { _getPropertyModel, makeObjectEmptyValueCreator, NumberModel, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Person from "./Person.js";
class PersonModel<T extends Person = Person> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PersonModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get email(): StringModel {
        return this[_getPropertyModel]("email", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
    get phoneNumber(): NumberModel {
        return this[_getPropertyModel]("phoneNumber", (parent, key) => new NumberModel(parent, key, false, { meta: { javaType: "int" } }));
    }
}
export default PersonModel;
