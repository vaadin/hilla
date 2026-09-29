import { _getPropertyModel, makeObjectEmptyValueCreator, ObjectModel, StringModel } from "@vaadin/hilla-lit-form";
import type Person from "./Person.js";
class PersonModel<T extends Person = Person> extends ObjectModel<T> {
    static override createEmptyValue = makeObjectEmptyValueCreator(PersonModel);
    get name(): StringModel {
        return this[_getPropertyModel]("name", (parent, key) => new StringModel(parent, key, true, { meta: { javaType: "java.lang.String" } }));
    }
}
export default PersonModel;
